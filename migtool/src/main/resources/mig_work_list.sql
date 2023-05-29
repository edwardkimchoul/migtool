CREATE TABLE UGENS.MIG_PROCESS_LIST {     
				PROCESS_ID              NUMBER(10)    PRIMARY KEY  
			, BUSINESS_UNIT           VARCHAR2(30)            /* 진료, 간호 */
			, MIG_WORKER_NAME         VARCHAR2(30)            /* 이행 개발자이름 */           
			,	PROCEDURE_NAME          VARCHAR2(30)            /* PROCEDURE_NAME  명명구칙에 따라  DELETE, INSERT, CREATE등   */ 
			,	SQL_TYPE_CD             VARCHAR2(1)             /* S: SQL, P : STORED PROCEDURE */
			,	PRECEDING_WORK_LIST     VARCHAR2(500)           /* 1,2,3,4,5,8,9 해당작업이 실행하기전 수행되어야 할 작업 List */
			,	MIG_STATUS_CD           VARCHAR2(1)             /* 수행전(R), 작업큐(Q), Processing(P), 작업완료(C), Error(E) */
			,	CRE_DATE                DATE   DEFAULT SYSDATE 
			,	UPD_DATE                DATE   DEFAULT SYSDATE
);


CREATE TABLE UGENS.MIG_PROCESS_HIST {
        MIG_HIST_NAME          VARCHAR2(20)   /* 이행한 날짜를 넣을것 2022-05-23  이행수행단위로 관리하기 위함  */    
			,	PROCESS_ID             NUMBER(10)
			,	MIG_START_TM           DATE           /* 시작일시 */
			,	MIG_END_TM             DATE
			,	PROCESS_SECOND         NUMBER(10)
			,	ROW_CNT                NUMBER(12)             /* 이행된 row count */
			,	ERROR_MESSAGE          VARCHAR2(4000)         /* 에러가 있으면 넣음 */
			, CRE_DATE               DATE   DEFAULT SYSDATE 
			, PRIMARY KEY ( MIG_HIST_NAME   , PROCESS_ID ) 
)
