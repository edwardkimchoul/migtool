DROP TABLE UGENS.CNV_PROCESS_LIST CASCADE CONSTRAINTS;
CREATE TABLE UGENS.CNV_PROCESS_LIST (     
				PROCESS_ID              VARCHAR2(10)    PRIMARY KEY
			,   PRIORITY_NO             NUMBER(10)	
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


DROP TABLE UGENS.CNV_PROCESS_HIST CASCADE CONSTRAINTS;
CREATE TABLE UGENS.CNV_PROCESS_HIST (
                ID                     NUMBER(10)   NOT NULL  PRIMARY KEY
            ,   MIG_HIST_NAME          VARCHAR2(20)   /* 이행한 날짜를 넣을것 2022-05-23  이행수행단위로 관리하기 위함  */    
			,	PROCESS_ID             VARCHAR2(10)
			,	START_TIME             VARCHAR2(30)           /* 시작일시 */
			,	STOP_TIME              VARCHAR2(30)
			,	EXECUTION_TIME         VARCHAR2(10)
			,	ROW_COUNT              VARCHAR2(50)             /* 이행된 row count */
			,	ERROR                  VARCHAR2(4000)         /* 에러가 있으면 넣음 */
			,   CRE_DATE               DATE   DEFAULT SYSDATE 
 
);

-- 			, constraint CNV_PROCESS_HIST_PK1 PRIMARY KEY ( MIG_HIST_NAME   , PROCESS_ID )
CREATE OR REPLACE FUNCTION UGENS.FN_MIG_NOT_COMPLETE_CNT (MIG_WORK_LIST VARCHAR2) 
    RETURN NUMBER 
IS 
    row_cnt NUMBER;    /* 미완료 갯수 */ 
BEGIN 
	
    SELECT count(1) CNT 
      INTO row_cnt 
      FROM CNV_PROCESS_LIST 
     WHERE PROCESS_ID IN ( SELECT REGEXP_SUBSTR(a.mig_work_list, '[^\|]+', 1, LEVEL) AS process_id
                             FROM (SELECT MIG_WORK_LIST AS mig_work_list 
                                     FROM dual) a
                           CONNECT BY LEVEL <= LENGTH(REGEXP_REPLACE(a.mig_work_list, '[^\|]+','')) + 1 )
      AND  MIG_STATUS_CD != 'C';                     

    RETURN row_cnt; 
END;
/  


SELECT M.PROCESS_ID, M.DB_NAME, M.PROCEDURE_NAME, M.SQL_TYPE_CD
  FROM CNV_PROCESS_LIST M
 WHERE M.MIG_STATUS_CD = 'R'   
   AND FN_MIG_NOT_COMPLETE_CNT( M.PRECEDING_WORK_LIST ) = 0
   ORDER BY PROCESS_ID;
   
--  타 시스템과 데이터 건수 비교 및 전 회차와 비교를 위해
--  ----------------------------------------------- 
DROP TABLE UGENS.CNV_DATA_COUNT CASCADE CONSTRAINTS;
CREATE TABLE UGENS.CNV_DATA_COUNT (     
                DATA_GROUP              VARCHAR2(20)    NOT NULL    /* STG, MAIN(전자동의서, EMR 포함)  */
			,	YYYYMMDD                VARCHAR2(9)     NOT NULL    /* 20230625 또는 20230701P  20230701S   (P: PRODUCT, S:STG) */
            ,   TABLE_NAME              VARCHAR2(100)   NOT NULL
			,   ROW_COUNT               NUMBER(20)
			,   constraint CNV_DATA_COUNT_PK1 PRIMARY KEY ( DATA_GROUP,  YYYYMMDD   , TABLE_NAME )
);

-- 비교대상 시스템 또는 회차와 비교 및 오차율 표시 
------------------------------------------------------------
SELECT *
FROM (
    SELECT TABLE_NAME, S_CNT, T_CNT, S_CNT -T_CNT DIFF, DECODE(S_CNT, 0, 0,TRUNC( (T_CNT - S_CNT) / S_CNT * 100,2)) DIFF_RATE
    FROM( 
        SELECT TABLE_NAME,
             MAX(CASE WHEN  SUBSTR(YYYYMMDD,9,1) = 'P'  THEN  ROW_COUNT
                  ELSE 0
             END)      T_CNT 
             ,MAX(CASE WHEN SUBSTR(YYYYMMDD,9,1) = 'S'  THEN  ROW_COUNT
                  ELSE 0
             END)      S_CNT 
          FROM CNV_DATA_COUNT
         WHERE SUBSTR(YYYYMMDD,1,8) BETWEEN TO_CHAR(SYSDATE-6, 'YYYYMMDD') AND TO_CHAR(SYSDATE, 'YYYYMMDD')  
           AND DATA_GROUP = 'STG'
        GROUP BY TABLE_NAME   
    )
)
ORDER BY DIFF_RATE DESC;


SELECT TABLE_NAME, S_CNT, T_CNT, DIFF, DIFF_RATE
FROM (
    SELECT TABLE_NAME, S_CNT, T_CNT, S_CNT -T_CNT DIFF, DECODE(S_CNT, 0, 0,TRUNC( (T_CNT - S_CNT) / S_CNT * 100,2)) DIFF_RATE
    FROM( 
        SELECT TABLE_NAME,
             MAX(CASE WHEN  SUBSTR(YYYYMMDD,1,8) BETWEEN TO_CHAR(SYSDATE-13, 'YYYYMMDD') AND TO_CHAR(SYSDATE-7, 'YYYYMMDD')  THEN  ROW_COUNT
                  ELSE 0
             END)      T_CNT 
             ,MAX(CASE WHEN SUBSTR(YYYYMMDD,1,8) BETWEEN TO_CHAR(SYSDATE-6, 'YYYYMMDD') AND TO_CHAR(SYSDATE, 'YYYYMMDD')  THEN  ROW_COUNT
                  ELSE 0
             END)      S_CNT 
          FROM CNV_DATA_COUNT
         WHERE DATA_GROUP = 'EMR'
        GROUP BY TABLE_NAME   
    )
)
ORDER BY DIFF_RATE DESC;
