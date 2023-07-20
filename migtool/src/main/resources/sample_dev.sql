  SELECT SUBSTR (E.REGISTRATIONDATE, 1, 4)                     "년월",
         MODALITY                                              "모달",
         ROUND (SUM (I.SIZEINSTS) / 1024 / 1024 / 1024, 2)     "용량"
    FROM EXAM E, IMAGELOCATION I
   WHERE     E.EXAMID = I.EXAMID
         AND E.REGISTRATIONDATE BETWEEN '20180101' AND '20221231'
         AND E.ACQUSTATUS = 'E'
         AND E.LABDEPT IN ('1108', '1114', '1112')
GROUP BY SUBSTR (E.REGISTRATIONDATE, 1, 4), MODALITY
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------


Predicate Information
Plan hash value
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------
| Id  | Operation                         | Name             | Starts | E-Rows | A-Rows |   A-Time   | Buffers | Reads  | Writes |  OMem |  1Mem | Used-Mem | Used-Tmp|
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------
|   0 | SELECT STATEMENT                  |                  |      1 |        |     23 |00:01:09.22 |    3714K|   4686K|    590K|       |       |          |         |
|   1 |  SORT GROUP BY                    |                  |      1 |  39200 |     23 |00:01:09.22 |    3714K|   4686K|    590K|  2048 |  2048 | 2048  (0)|         |
|*  2 |   HASH JOIN                       |                  |      1 |     64M|     92M|00:00:53.64 |    3714K|   4686K|    590K|   146M|    15M| 6587K (1)|    3910M|
|   3 |    PART JOIN FILTER CREATE        | :BF0000          |      1 |   1742K|   2236K|00:00:12.02 |   83520 |    557K|  91965 |       |       |          |         |
|*  4 |     VIEW                          | index$_join$_001 |      1 |   1742K|   2236K|00:00:11.61 |   83520 |    557K|  91965 |       |       |          |         |
|*  5 |      HASH JOIN                    |                  |      1 |        |   2236K|00:00:11.47 |   83520 |    557K|  91965 |   170M|    13M| 4963K (6)|     382M|
|*  6 |       HASH JOIN                   |                  |      1 |        |   2236K|00:00:04.86 |   47903 |    324K|  43470 |   349M|    28M|4654K (13)|     342M|
|*  7 |        INDEX RANGE SCAN           | EXAM_IDX6        |      1 |   1742K|   4936K|00:00:00.41 |   17204 |      0 |      0 |  1028K|  1028K|          |         |
|   8 |        INLIST ITERATOR            |                  |      1 |        |   7560K|00:00:02.36 |   30699 |  19069 |      0 |       |       |          |         |
|*  9 |         INDEX RANGE SCAN          | EXAM_IDX4        |      3 |   1742K|   7560K|00:00:01.94 |   30699 |  19069 |      0 |  1028K|  1028K|          |         |
|  10 |       INDEX STORAGE FAST FULL SCAN| EXAM_PK          |      1 |   1742K|     14M|00:00:00.85 |   35617 |     42 |      0 |  1028K|  1028K|          |         |
|  11 |    PARTITION RANGE JOIN-FILTER    |                  |      1 |    382M|    215M|00:00:25.34 |    3630K|   3630K|      0 |       |       |          |         |
|  12 |     TABLE ACCESS STORAGE FULL     | IMAGELOCATION    |      3 |    382M|    215M|00:00:12.79 |    3630K|   3630K|      0 |  1028K|  1028K| 3096K (0)|         |
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------

Predicate Information (identified by operation id):
---------------------------------------------------
 
   2 - access("E"."EXAMID"="I"."EXAMID")
   4 - filter(("E"."REGISTRATIONDATE">='20180101' AND "E"."ACQUSTATUS"='E' AND INTERNAL_FUNCTION("E"."LABDEPT") AND "E"."REGISTRATIONDATE"<='20221231'))
   5 - access(ROWID=ROWID)
   6 - access(ROWID=ROWID)
   7 - access("E"."REGISTRATIONDATE">='20180101' AND "E"."ACQUSTATUS"='E' AND "E"."REGISTRATIONDATE"<='20221231')
   9 - access(("E"."LABDEPT"='1108' OR "E"."LABDEPT"='1112' OR "E"."LABDEPT"='1114'))
   
   

--------------------------------------------------------------------------------------------------------
 [ EXAM - INDEX ]
--------------------------------------------------------------------------------------------------------
CREATE UNIQUE INDEX PKGDBA.EXAM_PK ON PKGDBA.EXAM (EXAMID)
CREATE INDEX PKGDBA.EXAM_IDX1 ON PKGDBA.EXAM (EXAMSTATUS, ROOMCODE, MODALITY, PTLOCATIONGROUP, EXAMDATE)
CREATE INDEX PKGDBA.EXAM_IDX10 ON PKGDBA.EXAM (DICTATEDDATE, DICTATORUSERID, EXAMSTATUS)
CREATE INDEX PKGDBA.EXAM_IDX11 ON PKGDBA.EXAM (APPROVEDDATE, APPROVERUSERID, DICTATORUSERID)
CREATE INDEX PKGDBA.EXAM_IDX2 ON PKGDBA.EXAM (HISPTID)
CREATE INDEX PKGDBA.EXAM_IDX3 ON PKGDBA.EXAM (EXAMDATE, MODALITY, VERIFIED, MACHINEID)
CREATE INDEX PKGDBA.EXAM_IDX4 ON PKGDBA.EXAM (LABDEPT, EXAMYEAR, ORDERCODECLASS, WORKNOSEQ)
CREATE INDEX PKGDBA.EXAM_IDX5 ON PKGDBA.EXAM (GROUPYEAR, GROUPINSP, GROUPSQTN)
CREATE INDEX PKGDBA.EXAM_IDX6 ON PKGDBA.EXAM (REGISTRATIONDATE, ACQUSTATUS, MODALITY)
CREATE INDEX PKGDBA.EXAM_IDX7 ON PKGDBA.EXAM (EXAMDATE, PTLOCATIONGROUP, CLINICIANUSERID)
CREATE INDEX PKGDBA.EXAM_IDX8 ON PKGDBA.EXAM (HISPTID, ACQUSTATUS, WORKNOSEQ)
--------------------------------------------------------------------------------------------------------