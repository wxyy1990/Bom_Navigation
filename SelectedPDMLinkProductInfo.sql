drop table SelectedPDMLinkProductInfo;

Create table SelectedPDMLinkProductInfo(
    sPDNo number primary key,
    pdID Varchar2(200),
    pdName Varchar2(200),
    pdXH Varchar2(200),
    userName	Varchar2(200)
);


create sequence SEQ_SELECTEDPDINFO INCREMENT BY 1 START WITH 1 NOMAXvalue  NOCYCLE CACHE 10;