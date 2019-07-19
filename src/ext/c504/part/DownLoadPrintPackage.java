package ext.c504.part;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.apache.log4j.Logger;

import com.bjsasc.platform.fileservice.client.FileTransmit;
import com.bjsasc.platform.fileservice.util.FileServerAPI;
import com.cascc.avidm.docman.integration.productmgr.web.DmProductMgrDelegateForWeb;
import com.cascc.avidm.docman.model.DmFile;
import com.cascc.avidm.docman.model.DmFileServer;

import wt.content.ApplicationData;
import wt.content.ContentServerHelper;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTStandardDateFormat;


public class DownLoadPrintPackage implements RemoteAccess{
	private static final Logger logger = Logger.getLogger(DownLoadPrintPackage.class);
	protected PageContext pageContext;
	protected HttpSession session;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected static PrintWriter out = null;
	OutputStream os;

	String oid = null;
	String downloadType = "";
	static File wttempDir;
	private static final String EOL = "\r\n";
	
	static {
		try {
			String wttemp = WTProperties.getLocalProperties().getProperty("wt.temp");
			wttempDir = wttemp == null ? null : new File(wttemp);
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	public DownLoadPrintPackage(PageContext pc) throws Exception {
		pageContext = pc;
		session = pc.getSession();
		request = (HttpServletRequest) pc.getRequest();
		response = (HttpServletResponse) pc.getResponse();
		os = response.getOutputStream();
//		out = new PrintWriter(new OutputStreamWriter(os, response.getCharacterEncoding()), true);
//		oid = request.getParameter("oid");
//		String type = request.getParameter("type");
//		if(type!=null && type.trim().length()>0){
//			downloadType = type;
//		}
	}
	
	
	
	public void downLoadFile(ArrayList list) throws Exception{
		File file=writeDownLoadPath(list);	
		if(file.exists()){
			try {
				String fname = new String(file.getName().getBytes("gb18030"), "iso-8859-1");

				response.setContentType("application/octet-stream");
				response.setContentLength((int) file.length());
				response.setHeader("Content-Disposition", "attachment; filename=\""
						+ fname + "\"");

				InputStream is = new FileInputStream(file);
				byte[] buf = new byte[1024];
				int len = 0;
				while ((len = is.read(buf)) >= 0)
					os.write(buf, 0, len);
				os.flush();
			} finally {
				if (file != null)
					file.delete();
			}
		}
	}
	
	public static File writeDownLoadPath(ArrayList list) throws Exception{
		if (!RemoteMethodServer.ServerFlag) {
			return (File) RemoteMethodServer.getDefault().invoke(
					"writeDownLoadPath", DownLoadPrintPackage.class.getName(), null,
					new Class[] { ArrayList.class}, new Object[] {list});
		}else{
			
			String parentFolderPath=wttempDir+File.separator+SessionHelper.getPrincipal().getName()+File.separator;
			deleteFile(parentFolderPath,1);
			
			//1.下载缓存文件
			Date now = new Date();
			Locale locale = Locale.CHINA;
			TimeZone timezone = TimeZone.getTimeZone("GMT+8:00");
			String nowTime = WTStandardDateFormat.format(now,"yyyy-MM-dd_HH.mm.ss", locale, timezone); 
			parentFolderPath=parentFolderPath+nowTime;
			mkdir(parentFolderPath);
			
			String fileName=downloadFileToTemppath(list,parentFolderPath);
			if(list.size()==1){
				return new File(parentFolderPath+File.separator+fileName);
			}else{
				Compressor book = new Compressor();
			    book.zip(parentFolderPath, parentFolderPath+".zip");  
			    deleteFile(parentFolderPath);
			    
			    return new File(parentFolderPath+".zip");
			}
			
			/*
			Iterator itr=map.keySet().iterator();
			String filename="";
			while(itr.hasNext()){
				String key=(String)itr.next();
				ApplicationData appData=(ApplicationData)map.get(key);
				filename = appData.getFileName();
				getTempFilePath(appData,fullPath+File.separator+filename);
			}
			if(map.size()==1){
				return new File(fullPath+File.separator+filename);
			}else{
				Compressor book = new Compressor();
			    book.zip(fullPath, fullPath+".zip");   
			    return new File(fullPath+".zip");
			}*/
			
		}
		
	}
	/**
	 * 删除文件夹的所有文件
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static boolean deleteFile(String dir) throws Exception {
		boolean delete = true;
		try {
			File f = new File(dir);
			if (f.isDirectory()) {
				File[] files = f.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].isFile()) {
							files[i].delete();
					} else if (files[i].isDirectory()) {
						deleteFile(files[i].getPath());
					}
				}
			}
			f.delete();

		} catch (Exception e) {
			delete = false;
			Debug.E(e);
		}
		return delete;
	}
	
	public static String downloadFileToTemppath(List fileList, String parentFolderPath) {
			// 检查文件夹是否存在，不存在则新建
		String fileName="";
		for (int i = 0; i < fileList.size(); i++) {
			Object obj = fileList.get(i);
			if (obj instanceof DmFile) {
				DmFile tempFile = (DmFile) obj;
//				File tempFolderPath = new File(parentFolderPath+tempFile.fileExtendName.substring(1)+File.separator);
//				if (!tempFolderPath.exists()) {
//					tempFolderPath.mkdirs();
//				}
				fileName=downloadEachFile(tempFile, parentFolderPath+File.separator);
			}
		}
		return fileName;
	}
	
	/**
	 * 单个文件下载
	 * 
	 * @param tempFile
	 *            需要下载的文件对象
	 * @param filePath
	 *            文件保存的物理路径
	 */
	public static String downloadEachFile(DmFile tempFile, String filePath) {
		// 文件名
		String fileName = "";
		if (tempFile == null) {
			return fileName;
		}
		// 获取FileServer
		DmFileServer fileServer = null;
		DmProductMgrDelegateForWeb productMgr = new DmProductMgrDelegateForWeb();
		try {
			String realFileName = tempFile.fileOriginalName.concat(tempFile.fileExtendName);
			if (tempFile.storageInfo.fileServer != null) {
				fileServer = tempFile.storageInfo.fileServer;
			} else {
				fileServer = productMgr.getFileServerBy(tempFile.productIID, tempFile.storageInfo.getLibrary());
			}
			if (fileServer != null && FileServerAPI.validateFileServer(fileServer.IP, fileServer.port)) {
				fileName = FileTransmit.downloadFile(fileServer.IP, fileServer.port, tempFile.storageInfo.storageIID,
						filePath, realFileName, fileServer.cabinet);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return fileName;
	}
	
	public static boolean deleteFile(String dir, int day) throws Exception {
		boolean delete = true;
		try {
			File f = new File(dir);
			if (f.isDirectory()) {
				File[] files = f.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].isFile()) {
						long time = System.currentTimeMillis()
								- files[i].lastModified();
						time = time / (1000 * 3600 * 24);
						if (time > day)
							files[i].delete();
					} else if (files[i].isDirectory()) {
						deleteFile(files[i].getPath(), day);
					}
				}
			}

		} catch (Exception e) {
			delete = false;
			Debug.E(e);
		}
		return delete;
	}
	
	/**
	 * 创造文件夹
	 * @param mkdirName
	 */
	public static void mkdir(String mkdirName) {
		try {
			File dirFile = new File(mkdirName);
			boolean bFile = dirFile.exists();
			if (bFile == true) {
				Debug.P("the folder exists");
			} else {
				bFile = dirFile.mkdirs();
				if (bFile == true) {
					Debug.P("创建文件夹成功");
				} else {
					Debug.P("创建文件夹失败，请确认磁盘没有写保护并且有足够空间");
				}
			}
		} catch (Exception err) {
			System.err.println("创建文件夹发生异常");
			err.printStackTrace();
		}
	}
	
	/**
	 * 得到临时文件的路径
	 * @param number
	 * @param applicationdata
	 * @return
	 * @throws IOException
	 * @throws WTException
	 */
	public static String getTempFilePath(ApplicationData applicationdata,String fullPath) throws IOException, WTException{
		InputStream stream = ContentServerHelper.service.findLocalContentStream(applicationdata);
		
		Debug.P("========>fullPath: " + fullPath+" == "+stream);
		
		OutputStream os = new BufferedOutputStream(new FileOutputStream(fullPath));
        byte[] buf = new byte[1024];
        int len = 0;
        while ((len = stream.read(buf)) >= 0) {
             os.write(buf, 0, len);
        }
        os.flush();
        os.close();
		
            
        return fullPath;
	}
}