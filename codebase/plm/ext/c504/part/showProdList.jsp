 
 
 
 
 
 
 
 
<html>
<body>
 
<link rel="stylesheet" type="text/css" href="/avidm/javascript/ext/resources/css/ext-all.css" /> 
<script type="text/javascript" src="/avidm/javascript/ext/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="/avidm/javascript/ext/ext-all-debug.js"></script>
<script type="text/javascript" src="/avidm/javascript/ext/CollectGarbage.js"></script>
<link rel="stylesheet" type="text/css" href="/avidm/javascript/ext/resources/css/ext-sasc.css" /> 
<script type="text/javascript" src="/avidm/javascript/ext/source/locale/ext-lang-zh_CN.js" charset="GBK"></script>
<script type="text/javascript" src="/avidm/platform/common/js/ptutil.js" charset="GBK"></script>
<script type="text/javascript" src="/avidm/platform/common/js/ptui/date/date.js" charset="GBK"></script>
 
 
<script type="text/javascript" src="/avidm/javascript/ext/ext-sasc.js" charset="gb2312"></script>
 
<script> 
 var clientWidth = 1024; 
var clientHeight = 600;
 clientWidth=document.body.clientWidth;
 clientHeight=document.body.clientHeight; Ext.BLANK_IMAGE_URL = '/avidm/javascript/ext/resources/images/default/s.gif';
 
 Ext.onReady(function(){	
    Ext.QuickTips.init();//��ʼ��������ʾ��չʾ��
    Ext.form.Field.prototype.msgTarget = 'qtip';//��ʾ���������Χ��������״��ʾ
 
     Ext.getBody().mask("�������¼����У����Ե�");    
		
		
		
		
		
		
    var sm = new Ext.grid.CheckboxSelectionModel();
    var grid_columnModel = new Ext.grid.ColumnModel([
new Ext.grid.RowNumberer(),sm
,{header:"��Ʒ��ʶ" 
,dataIndex:"PRODIID",sortable:true
,hidden:true,alwaysHidden:true}
,{header:"��ʶ" 
,dataIndex:"PRODID",sortable:false
,renderer:function(value,p,record,rowIndex){ p.attr = 'ext:qtip="'  + value + '"';return value;}}
,{header:"����" 
,dataIndex:"PRODNAME",sortable:false
,renderer:function(value,p,record,rowIndex){ p.attr = 'ext:qtip="'  + value + '"';return value;}}
,{header:"����" 
,dataIndex:"PRODTYPE",sortable:true
,hidden:true,alwaysHidden:true}
,{header:"��ѡ" 
,dataIndex:"DISPLAY",sortable:true
,hidden:true,alwaysHidden:true}
    ]);
    grid_columnModel.defaultSortable = true;
//========== ���ݴ洢������ ==========
    var grid_extDstore = new Ext.data.Store({
        baseParams : {InitCond: ""},
        listeners : {loadexception:function(httpProxy, dataObject, arguments, exception){var obj= Ext.decode(arguments.responseText);var str =obj.detail;
var form;
var ww = new Ext.Window({
width : 500,autoHeight:true,autoScroll :true,resizable : false,modal:true,plain : true,
items : [form = new Ext.FormPanel({labelWidth: 70,
frame:true,title: obj.title,bodyStyle:'padding:5px 5px 0',width: '100%',height : '100%',
 items: [{colspan:2,html:'&nbsp;&nbsp;&nbsp;'+obj.info+'<br><br>'},{
xtype:'fieldset',title: '��ϸ��Ϣ',collapsible: true,collapsed:true,height : 300,layout:'table',
 items :[new  Ext.form.TextArea({name: 'error',width: 400,height : 250, value: str})],
listeners:{expand:function(p){form.getField('error').setReadOnly(true);form.getField('error').focus();}}
 }]})]});ww.show();
}},
        proxy : new Ext.data.HttpProxy({method: "POST",url:"getProdList.jsp?op=list"}),
        reader:new Ext.data.JsonReader(
            {root: "TABLE", totalProperty: "totalCount" },
            Ext.data.Record.create([
{name: "PRODIID",mapping:"PRODIID"},{name: "PRODID",mapping:"PRODID"},{name: "PRODNAME",mapping:"PRODNAME"},{name: "PRODTYPE",mapping:"PRODTYPE"},{name: "DISPLAY",mapping:"DISPLAY"}            ])),
            sortInfo : {field: "", direction: ""},
            remoteSort : true
        });
//========== ���巭ҳ�� ==========
        grid_pageToolBar = new Ext.PagingToolbar({
            pageSize : 10,
            store : grid_extDstore,
            displayInfo : true,
            displayMsg : " {0} - {1} / {2}",
            emptyMsg : '',
            items:[
                new Ext.Toolbar.Spacer(),
                grid_pageSizeBox = new Ext.form.ComboBox({
                    width : 50,
                    valueField : "id",
                    displayField : "text",
                    store:new Ext.data.SimpleStore({
                        fields : ["id", "text"],
                        data : [[5,"5"],[10,"10"],[15,"15"],[20,"20"]]
                    }),
                    mode : "local",
                    triggerAction : "all",
                    emptyText : 10,
                    enableKeyEvents : true,
                    listeners:{
                        keydown : function(e){var keyCode;if(document.all){keyCode = event.keyCode;}else{keyCode = e.which;} if(keyCode==13) { var partn=/^[0-9]*[1-9][0-9]*$/; if(!partn.exec(parseInt(this.el.dom.value))){Ext.Msg.alert('��ʾ', '������������.');grid_pageSizeBox.setValue(10);this.fireEvent("select",this);return;}grid_pageSizeBox.setValue(parseInt(this.el.dom.value)); this.fireEvent("select",this);}},
                        blur : function(){ var partn=/^[0-9]*[1-9][0-9]*$/; if(!partn.exec(parseInt(this.el.dom.value))){Ext.Msg.alert('��ʾ', '������������.');grid_pageSizeBox.setValue(10);this.fireEvent("select",this);return;}grid_pageSizeBox.setValue(parseInt(this.el.dom.value)); this.fireEvent("select",this);}
                    }
                })
            ]
        });
//========== ����GridPanel ==========
        grid_extGrid = new Ext.grid.GridPanel({
            id : 'grid',
            sm : sm,
            store : grid_extDstore,
            cm : grid_columnModel,
            monitorResize : true,
            doLayout : function(shallow){//�ڴ��ڸı��Сʱ������
                this.setSize({width:Ext.get("g").getWidth(),height:Ext.get("g").getHeight()}); //ռ��div
                Ext.grid.GridPanel.superclass.doLayout.call(this,shallow);
            },
            filterPrefix : false,
            plugins:[ 
            ],
            frame : true
        });
//==========  ��ȡdataStore ==========
        grid_extDstore.load({params:{start:0,limit:10},callback : function(options, success, response){ Ext.getBody().unmask();  }});
grid_extDstore.on("load", function( ){ var obj = getElements('x-grid3-hd-checker-on', 'DIV', document);if (obj[0]) {obj[0].className = 'x-grid3-hd-checker';}}); 
grid_pageSizeBox.on("select",function(){ var size = grid_pageSizeBox.getValue();pagesize = size;grid_pageToolBar.pageSize = size;grid_extDstore.load({params:{start:0,limit:size}});});
        grid_extGrid.region='center';
//========== ��Grid����Panel�� ==========
        grid_g_extPanel=new Ext.Panel({
            id : 'grid_panel',
            layout : 'border',
            monitorResize : true,
            doLayout : function(shallow){//�ڴ��ڸı��Сʱ������
                this.setSize(Ext.get("g").getSize()); //ռ��div
                Ext.Panel.superclass.doLayout.call(this,shallow);
            },
            items : grid_extGrid
        });
//========== ��Ⱦ��ָ��div ==========
        grid_g_extPanel.setSize(Ext.get('g').getSize());
        grid_g_extPanel.render("g");
 
 
 
 
	var button_4214=new Ext.Button({text:"ȷ��",id:"button_4214",iconCls:"add",cls:'x-btn-text-icon',disabled:false,handler:confirmProd});
 
	var button_9913=new Ext.Button({text:"ȡ��",id:"button_9913",iconCls:"delete",cls:'x-btn-text-icon',disabled:false,handler:cancel});
 
//========== ����Panel ==========
    var bar1_toolbar = new Ext.Panel({
        frame : true,
        monitorResize : true,
        doLayout : function(shallow){//�ڴ��ڸı��Сʱ������
            this.setSize(Ext.get("f").getSize()); //ռ��div
            Ext.Panel.superclass.doLayout.call(this,shallow);
        },
        layout : 'ux.horizontal',
        items : [button_4214,button_9913]
    });
    
    bar1_toolbar.setSize(Ext.get('f').getSize());
//========== ��ȾToolbar��ָ��div ==========
bar1_toolbar.render('f');
	
});
var grid_extGrid;var grid_g_extPanel;var grid_filters = new Ext.ux.grid.GridFilter(); ; var grid_pageToolBar,grid_pageSizeBox; function grid_openConditionWin(columnNum,objID){   grid_filters.openConditionWin(columnNum,objID); } function grid_refreshFunc(){ grid_pageToolBar.doLoad(0);};
</script>
 
 
 
 
<script type="text/javascript"> 
function confirmProd(){
	var selections = grid_extGrid.getSelectedRow();
	var records = "";
	for(var i = 0 ; i < selections.length ; i++){
		var newRecord = selections[i].copy();
		records= records+";"+newRecord.data.PRODIID;
		//records= records+";"+newRecord.data.PRODIID+"|"+newRecord.data.PRODID+"-"+newRecord.data.PRODNAME;
	}
	if(records==""){
		alert("��ѡ���Ʒ");
		return;
	}
	
	grid_extGrid.clearSelections();
	//console.log(records);
	parent.opener.sendProdList(records);
	parent.window.close();
}
 
function cancel(){
	parent.window.close();
}
 
</script>
 
 
	<table width="100%" height="100%" border="0">
		<tr height="90%">
			<td width="100%"><div id="g" style="height:100%;width:100%;"></div></td>
		</tr>
		
		<tr height="10%">
			<td align="center"><div id="f" style="height:100%;width:100%;"></div></td>
		</tr>		
	</table>
</body>
</html>
