
/********************************************
 * Select Permissions for User
 ********************************************/
SELECT P.*
FROM CFW_PERMISSION P
JOIN CFW_ROLE_PERMISSION_MAP AS GP ON GP.FK_ID_PERMISSION = P.PK_ID
JOIN CFW_USER_GROUP_MAP AS UG ON UG.FK_ID_ROLE = GP.FK_ID_ROLE
WHERE UG.FK_ID_USER = 3;

/********************************************
 * Permissions overview
 ********************************************/
SELECT U.USERNAME, G.NAME AS GROUPNAME, P.NAME AS PERMISSION 
FROM CFW_USER U
LEFT JOIN CFW_USER_GROUP_MAP AS UG ON UG.FK_ID_USER = U.PK_ID
LEFT JOIN CFW_GROUP AS G ON UG.FK_ID_ROLE = G.PK_ID
LEFT JOIN CFW_ROLE_PERMISSION_MAP AS GP ON GP.FK_ID_ROLE = G.PK_ID 
LEFT JOIN CFW_PERMISSION AS P ON GP.FK_ID_PERMISSION = P.PK_ID
ORDER BY LOWER(U.USERNAME), LOWER(G.NAME), LOWER(P.NAME)