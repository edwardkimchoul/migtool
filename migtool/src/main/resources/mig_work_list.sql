DROP TABLE UGENS.MIG_PROCESS_LIST CASCADE CONSTRAINTS;
CREATE TABLE UGENS.MIG_PROCESS_LIST (     
				PROCESS_ID              NUMBER(10)    PRIMARY KEY  
			,   BUSINESS_UNIT           VARCHAR2(30)            /* 진료, 간호 */
			,   MIG_WORKER_NAME         VARCHAR2(30)            /* 이행 개발자이름 */           
			,	PROCEDURE_NAME          VARCHAR2(30)            /* PROCEDURE_NAME  명명구칙에 따라  DELETE, INSERT, CREATE등   */ 
			,	SQL_TYPE_CD             VARCHAR2(1)             /* S: SQL, P : STORED PROCEDURE */
			,   MIG_DB_NAME             VARCHAR2(20)            /* Migration 할 DB 이름 */
			,	PRECEDING_WORK_LIST     VARCHAR2(500)           /* 1,2,3,4,5,8,9 해당작업이 실행하기전 수행되어야 할 작업 List */
			,	MIG_STATUS_CD           VARCHAR2(1)             /* 수행전(R), 작업큐(Q), Processing(P), 작업완료(C), Error(E) */
			,	CRE_DATE                DATE   DEFAULT SYSDATE 
			,	UPD_DATE                DATE   DEFAULT SYSDATE
);


DROP TABLE UGENS.MIG_PROCESS_HIST CASCADE CONSTRAINTS;
CREATE TABLE UGENS.MIG_PROCESS_HIST (

        MIG_HIST_NAME          VARCHAR2(20)   /* 이행한 날짜를 넣을것 2022-05-23  이행수행단위로 관리하기 위함  */    
			,	PROCESS_ID             NUMBER(10)
			,	START_TIME             VARCHAR2(30)           /* 시작일시 */
			,	STOP_TIME              VARCHAR2(30)
			,	EXECUTION_TIME         NUMBER(10)
			,	ROW_COUNT              NUMBER(12)             /* 이행된 row count */
			,	ERROR                  VARCHAR2(4000)         /* 에러가 있으면 넣음 */
			, CRE_DATE               DATE   DEFAULT SYSDATE 
			, constraint MIG_PROCESS_HIST_PK1 PRIMARY KEY ( MIG_HIST_NAME   , PROCESS_ID ) 
);


CREATE OR REPLACE FUNCTION FN_MIG_NOT_COMPLETE_CNT (MIG_WORK_LIST VARCHAR2) 
    RETURN NUMBER 
IS 
    row_cnt NUMBER;    /* 미완료 갯수 */ 
BEGIN 
	
    SELECT count(1) CNT 
      INTO row_cnt 
      FROM MIG_PROCESS_LIST 
     WHERE PROCESS_ID IN ( SELECT REGEXP_SUBSTR(a.mig_work_list, '[^,]+', 1, LEVEL) AS process_id
                             FROM (SELECT MIG_WORK_LIST AS mig_work_list 
                                     FROM dual) a
                           CONNECT BY LEVEL <= LENGTH(REGEXP_REPLACE(a.mig_work_list, '[^|]+','')) + 1 )
      AND  STATUS != 'C'                     

    RETURN row_cnt; 
END;    


SELECT M.PROCESS_ID, M.DB_NAME, M.PROCEDURE_NAME, M.SQL_TYPE_CD
  FROM MIG_PROCESS_LIST M
 WHERE M.MIG_STATUS_CD = 'R'   
   AND FN_MIG_NOT_COMPLETE_CNT( M.PRECEDING_WORK_LIST ) = 0
   ORDER BY PROCESS_ID;
   

