 ȯ�� ����
==================================================================================
1) Grant ����
  grant query rewrite to [user];
  grant materialized  to [user];


2) ���� mview ��ȸ
-------------------------
select * from dba_mviews


========================================================================================================

1) dbms_advisor�� �̿��Ͽ� ���� Advice ����
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
 


2) Advice ���� ��ȸ
====================
select script_type, statement
from   dba_tune_mview
where  task_name = '�۾�_3385'
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




3) REWRITE_OR_ERROR ��Ʈ ���
==================================================================================
create materialized view mv_country refresh fast enable query
      rewrite as SELECT /*+ REWRITE_OR_ERROR */ COUNTRY_ID, COUNTRY_NAME, REGION_NAME
                   FROM COUNTRIES C, REGIONS R
                WHERE C.REGION_ID = R.REGION_ID



4) INDEX ���� ���� (Partition Index�� ������)
==================================================================================
create index IS_MV_COUNTRY_01  on MV_COUNTRY(COUNTRY_ID); 







5) MV_VIEW�� MV ����� 
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
���� : refresh ���� 
=========================================================================        

6) �� ó�� �����ð� ����  

 : REFRESH FAST START WITH SYSDATE+(1/(24*60)) NEXT (SYSDATE+(1/(24*60)))+(1/(24*60))
   1�а������� Refresh �� 
   
   ���� ������ �����϶��� ��ü �ٽ� �����ؾ� �ǳ� �ش� ������ �ʿ�
   �Ʒ��� ������ JOB������ ������ ������ �ϸ� Fast refresh �Ǵ� Complete�� ���� ������ ����
   -----------> fast refresh�� �������� ���� �޶��������� ������
   -----------> ��ǻ Ž���� ������ ���� ��å�� �ʿ��� 


7) MV_VIEW�� Refresh �ϱ�
==================================================================================
begin
DBMS_MVIEW.REFRESH('MV_LOCATION', method => '?',   atomic_refresh => FALSE, out_of_place => TRUE);
end;


Best option is to use the '?' argument for the method. This way DBMS_MVIEW will choose the best way to refresh, 
so it'll do the fastest refresh it can for you. , and won't fail 
if you try something like method=>'f' when you actually need a complete refresh. :-)




8) ������ MV refresh �ϱ�
==================================================================================
DECLARE numerrs PLS_INTEGER;
BEGIN DBMS_MVIEW.REFRESH_DEPENDENT (
   number_of_failures => numerrs, list=>'SALES', method => 'C');
DBMS_OUTPUT.PUT_LINE('There were ' || numerrs || ' errors during refresh');
END;
/




9) ����ó�� 
 /*+ REWRITE_OR_ERROR */ �� ���� ������ ǥ���ϰ� ó��
-----------------------------------------------
select /*+ REWRITE_OR_ERROR */ city, sum(actual_rate)
from hotels h, reservations r, trans t
where t.resv_id = r.resv_id
and h.hotel_id = r.hotel_id
group by city;

������ ������ ���� ������ ǥ�õ�
-----------------------

from hotels h, reservations r, trans t

     *

ERROR at line 2:

ORA-30393: a query block in the statement did not rewrite