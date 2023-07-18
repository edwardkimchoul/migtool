 환경 설정
==================================================================================
1) Grant 설정
  grant query rewrite to [user];
  grant materialized  to [user];


2) 현재 mview 조회
-------------------------
select * from dba_mviews


========================================================================================================

1) dbms_advisor를 이용하여 관련 Advice 받음
==================================================================================
var adv_name varchar2(20)

begin
dbms_advisor.tune_mview
   (
      :adv_name,
      'create materialized view mv_country refresh fast enable query
      rewrite as SELECT COUNTRY_ID, COUNTRY_NAME, REGION_NAME
                   FROM COUNTRIES C, REGIONS R
                WHERE C.REGION_ID = R.REGION_ID');
end;  


 print adv_name;
 


2) Advice 내용 조회
====================
select script_type, statement
from   dba_tune_mview
where  task_name = '작업_3385'
order  by script_type, action_id;

=======================================================================================================
IMPLEMENTATION
CREATE MATERIALIZED VIEW LOG ON "UGENS"."COUNTRIES" WITH ROWID                  
                                                                                
IMPLEMENTATION
ALTER MATERIALIZED VIEW LOG FORCE ON "UGENS"."COUNTRIES" ADD ROWID              
                                                                                
IMPLEMENTATION
CREATE MATERIALIZED VIEW LOG ON "UGENS"."REGIONS" WITH ROWID                    
                                                                                
IMPLEMENTATION
ALTER MATERIALIZED VIEW LOG FORCE ON "UGENS"."REGIONS" ADD ROWID                
                                                                                
IMPLEMENTATION
CREATE MATERIALIZED VIEW UGENS.MV_COUNTRY   REFRESH FAST WITH ROWID ENABLE QUERY REWRITE 
  AS SELECT UGENS.REGIONS.ROWID C1, UGENS.COUNTRIES.ROWID C2, 
            UGENS.COUNTRIES.COUNTRY_ID, UGENS.COUNTRIES.COUNTRY_NAME,
            UGENS.REGIONS.REGION_NAME 
     FROM UGENS.REGIONS, UGENS.COUNTRIES 
WHERE UGENS.COUNTRIES.REGION_ID = UGENS.REGIONS.REGION_ID;          
                                                                                

UNDO          
DROP MATERIALIZED VIEW "UGENS"."MV_COUNTRY"                                     




3) REWRITE_OR_ERROR 힌트 사용
==================================================================================
create materialized view mv_country refresh fast enable query
      rewrite as SELECT /*+ REWRITE_OR_ERROR */ COUNTRY_ID, COUNTRY_NAME, REGION_NAME
                   FROM COUNTRIES C, REGIONS R
                WHERE C.REGION_ID = R.REGION_ID



4) INDEX 등의 생성 (Partition Index도 가능함)
==================================================================================
create index IS_MV_COUNTRY_01  on MV_COUNTRY(COUNTRY_ID); 







5) MV_VIEW로 MV 만들기 
==================================================================================

DROP MATERIALIZED VIEW UGENS.MV_LOCATION   ;


CREATE MATERIALIZED VIEW LOG ON "UGENS"."MV_COUNTRY" WITH ROWID;                 
ALTER MATERIALIZED VIEW LOG FORCE ON "UGENS"."MV_COUNTRY" ADD ROWID;             
CREATE MATERIALIZED VIEW LOG ON "UGENS"."LOCATIONS" WITH ROWID;            
ALTER MATERIALIZED VIEW LOG FORCE ON "UGENS"."LOCATIONS" ADD ROWID;   

CREATE MATERIALIZED VIEW UGENS.MV_LOCATION   REFRESH FAST START WITH SYSDATE+(1/(24*60)) NEXT (SYSDATE+(1/(24*60)))+(1/(24*60))  WITH ROWID ENABLE QUERY REWRITE 
AS SELECT UGENS.LOCATIONS.ROWID C1, UGENS.MV_COUNTRY.ROWID C2, UGENS.LOCATIONS.CITY , UGENS.LOCATIONS.POSTAL_CODE , 
          UGENS.LOCATIONS.STREET_ADDRESS , UGENS.MV_COUNTRY.COUNTRY_NAME , 
           UGENS.MV_COUNTRY.REGION_NAME 
     FROM UGENS.LOCATIONS, UGENS.MV_COUNTRY 
    WHERE UGENS.MV_COUNTRY.COUNTRY_ID = UGENS.LOCATIONS.COUNTRY_ID  ;
    
=========================================================================        
참고 : refresh 전략 
=========================================================================        

6) 위 처럼 일정시간 동안  

 : REFRESH FAST START WITH SYSDATE+(1/(24*60)) NEXT (SYSDATE+(1/(24*60)))+(1/(24*60))
   1분간격으로 Refresh 함 
   
   만약 구조가 오류일때는 전체 다시 생성해야 되나 해당 전략이 필요
   아래의 방법대로 JOB같은데 정기적 수행을 하면 Fast refresh 또는 Complete로 되지 않을까 생각
   -----------> fast refresh의 안정성에 따라 달라질것으로 생각됨
   -----------> 오퓨 탐지와 복구에 대한 대책이 필요함 


7) MV_VIEW로 Refresh 하기
==================================================================================
begin
DBMS_MVIEW.REFRESH('MV_LOCATION', method => '?',   atomic_refresh => FALSE, out_of_place => TRUE);
end;


Best option is to use the '?' argument for the method. This way DBMS_MVIEW will choose the best way to refresh, 
so it'll do the fastest refresh it can for you. , and won't fail 
if you try something like method=>'f' when you actually need a complete refresh. :-)




8) 연관된 MV refresh 하기
==================================================================================
DECLARE numerrs PLS_INTEGER;
BEGIN DBMS_MVIEW.REFRESH_DEPENDENT (
   number_of_failures => numerrs, list=>'SALES', method => 'C');
DBMS_OUTPUT.PUT_LINE('There were ' || numerrs || ' errors during refresh');
END;
/




9) 오류처리 
 /*+ REWRITE_OR_ERROR */ 을 통해 오류를 표시하고 처리
-----------------------------------------------
select /*+ REWRITE_OR_ERROR */ city, sum(actual_rate)
from hotels h, reservations r, trans t
where t.resv_id = r.resv_id
and h.hotel_id = r.hotel_id
group by city;

오류시 다음과 같은 오류가 표시됨
-----------------------

from hotels h, reservations r, trans t

     *

ERROR at line 2:

ORA-30393: a query block in the statement did not rewrite