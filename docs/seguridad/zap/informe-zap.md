# PréstamoLab CTMA - revisión pasiva con OWASP ZAP

ZAP by [Checkmarx](https://checkmarx.com/).


## Summary of Alerts

| Risk Level | Number of Alerts |
| --- | --- |
| High | 0 |
| Medium | 0 |
| Low | 2 |
| Informational | 4 |




## Insights

| Level | Reason | Site | Description | Statistic |
| --- | --- | --- | --- | --- |
| Info | Informational | https://<proyecto>.supabase.co | Percentage of responses with status code 2xx | 89 % |
| Info | Informational | https://<proyecto>.supabase.co | Percentage of responses with status code 4xx | 10 % |
| Info | Informational | https://<proyecto>.supabase.co | Percentage of endpoints with content type application/json | 80 % |
| Info | Informational | https://<proyecto>.supabase.co | Percentage of endpoints with method DELETE | 7 % |
| Info | Informational | https://<proyecto>.supabase.co | Percentage of endpoints with method GET | 53 % |
| Info | Informational | https://<proyecto>.supabase.co | Percentage of endpoints with method PATCH | 11 % |
| Info | Informational | https://<proyecto>.supabase.co | Percentage of endpoints with method POST | 26 % |
| Info | Informational | https://<proyecto>.supabase.co | Count of total endpoints | 26    |
| Info | Informational | https://<proyecto>.supabase.co | Percentage of slow responses | 100 % |







## Alerts

| Name | Risk Level | Number of Instances |
| --- | --- | --- |
| Cookie with SameSite Attribute None | Low | Systemic |
| Timestamp Disclosure - Unix | Low | Systemic |
| Information Disclosure - Sensitive Information in URL | Informational | 2 |
| Loosely Scoped Cookie | Informational | Systemic |
| Re-examine Cache-control Directives | Informational | Systemic |
| Session Management Response Identified | Informational | 27 |




## Alert Detail



### [ Cookie with SameSite Attribute None ](https://www.zaproxy.org/docs/alerts/10054/)



##### Low (Medium)

### Description

A cookie has been set with its SameSite attribute set to "none", which means that the cookie can be sent as a result of a 'cross-site' request. The SameSite attribute is an effective counter measure to cross-site request forgery, cross-site script inclusion, and timing attacks.

* URL: https://<proyecto>.supabase.co/rest/v1/equipments%3Fselect=id,name,category,status&order=name
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/equipments (order,select)`
  * Method: `GET`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `set-cookie: __cf_bm`
  * Other Info: ``
* URL: https://<proyecto>.supabase.co/rest/v1/loans%3Fselect=id,user_id,equipment_id,status,request_date,return_date,environment,purpose,duration_hours,reviewed_by,rejection_reason,latitude,longitude,solicitante:users!loans_user_id_fkey(full_name&29&order=request_date
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/loans (order,select)`
  * Method: `GET`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `set-cookie: __cf_bm`
  * Other Info: ``
* URL: https://<proyecto>.supabase.co/rest/v1/returns%3Fselect=id,loan_id,equipment_condition,notes,return_date,latitude,longitude
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/returns (select)`
  * Method: `GET`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `set-cookie: __cf_bm`
  * Other Info: ``
* URL: https://<proyecto>.supabase.co/rest/v1/rpc/iniciar_sesion
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/rpc/iniciar_sesion ()({p_identificador,p_hash})`
  * Method: `POST`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `set-cookie: __cf_bm`
  * Other Info: ``
* URL: https://<proyecto>.supabase.co/rest/v1/rpc/sesion_valida
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/rpc/sesion_valida ()({})`
  * Method: `POST`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `set-cookie: __cf_bm`
  * Other Info: ``

Instances: Systemic


### Solution

Ensure that the SameSite attribute is set to either 'lax' or ideally 'strict' for all cookies.

### Reference


* [ https://datatracker.ietf.org/doc/html/draft-ietf-httpbis-cookie-same-site ](https://datatracker.ietf.org/doc/html/draft-ietf-httpbis-cookie-same-site)


#### CWE Id: [ 1275 ](https://cwe.mitre.org/data/definitions/1275.html)


#### WASC Id: 13

#### Source ID: 3

### [ Timestamp Disclosure - Unix ](https://www.zaproxy.org/docs/alerts/10096/)



##### Low (Low)

### Description

A timestamp was disclosed by the application/web server. - Unix

* URL: https://<proyecto>.supabase.co/rest/v1/equipments%3Fselect=id,name,category,status&order=name
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/equipments (order,select)`
  * Method: `GET`
  * Parameter: `set-cookie`
  * Attack: ``
  * Evidence: `1790304095`
  * Other Info: `1790304095, which evaluates to: 2026-09-24 21:41:35.`
* URL: https://<proyecto>.supabase.co/rest/v1/loans%3Fselect=id,user_id,equipment_id,status,request_date,return_date,environment,purpose,duration_hours,reviewed_by,rejection_reason,latitude,longitude,solicitante:users!loans_user_id_fkey(full_name&29&order=request_date
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/loans (order,select)`
  * Method: `GET`
  * Parameter: `set-cookie`
  * Attack: ``
  * Evidence: `1790304095`
  * Other Info: `1790304095, which evaluates to: 2026-09-24 21:41:35.`
* URL: https://<proyecto>.supabase.co/rest/v1/returns%3Fselect=id,loan_id,equipment_condition,notes,return_date,latitude,longitude
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/returns (select)`
  * Method: `GET`
  * Parameter: `set-cookie`
  * Attack: ``
  * Evidence: `1790304096`
  * Other Info: `1790304096, which evaluates to: 2026-09-24 21:41:36.`
* URL: https://<proyecto>.supabase.co/rest/v1/rpc/iniciar_sesion
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/rpc/iniciar_sesion ()({p_identificador,p_hash})`
  * Method: `POST`
  * Parameter: `set-cookie`
  * Attack: ``
  * Evidence: `1790304094`
  * Other Info: `1790304094, which evaluates to: 2026-09-24 21:41:34.`
* URL: https://<proyecto>.supabase.co/rest/v1/rpc/sesion_valida
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/rpc/sesion_valida ()({})`
  * Method: `POST`
  * Parameter: `set-cookie`
  * Attack: ``
  * Evidence: `1790304095`
  * Other Info: `1790304095, which evaluates to: 2026-09-24 21:41:35.`

Instances: Systemic


### Solution

Manually confirm that the timestamp data is not sensitive, and that the data cannot be aggregated to disclose exploitable patterns.

### Reference


* [ https://cwe.mitre.org/data/definitions/200.html ](https://cwe.mitre.org/data/definitions/200.html)


#### CWE Id: [ 497 ](https://cwe.mitre.org/data/definitions/497.html)


#### WASC Id: 13

#### Source ID: 3

### [ Information Disclosure - Sensitive Information in URL ](https://www.zaproxy.org/docs/alerts/10024/)



##### Informational (Medium)

### Description

The request appeared to contain sensitive information leaked in the URL. This can violate PCI and most organizational compliance policies. You can configure the list of strings for this check to add or remove values specific to your environment.

* URL: https://<proyecto>.supabase.co/rest/v1/loans%3Fselect=id,user_id,equipment_id,status,request_date,return_date,environment,purpose,duration_hours,reviewed_by,rejection_reason,latitude,longitude,solicitante:users!loans_user_id_fkey(full_name&29&order=request_date&user_id=eq.9098a9df-41f6-45a5-91dd-e032ebd6892d
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/loans (order,select,user_id)`
  * Method: `GET`
  * Parameter: `user_id`
  * Attack: ``
  * Evidence: `user_id`
  * Other Info: `The URL contains potentially sensitive information. The following string was found via the pattern: user
user_id`
* URL: https://<proyecto>.supabase.co/rest/v1/returns%3Fselect=id,loan_id,equipment_condition,notes,return_date,latitude,longitude,loans!inner(user_id&29&loans.user_id=eq.9098a9df-41f6-45a5-91dd-e032ebd6892d
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/returns (loans.user_id,select)`
  * Method: `GET`
  * Parameter: `loans.user_id`
  * Attack: ``
  * Evidence: `loans.user_id`
  * Other Info: `The URL contains potentially sensitive information. The following string was found via the pattern: user
loans.user_id`


Instances: 2

### Solution

Do not pass sensitive information in URIs.

### Reference



#### CWE Id: [ 598 ](https://cwe.mitre.org/data/definitions/598.html)


#### WASC Id: 13

#### Source ID: 3

### [ Loosely Scoped Cookie ](https://www.zaproxy.org/docs/alerts/90033/)



##### Informational (Low)

### Description

Cookies can be scoped by domain or path. This check is only concerned with domain scope.The domain scope applied to a cookie determines which domains can access it. For example, a cookie can be scoped strictly to a subdomain e.g. www.nottrusted.com, or loosely scoped to a parent domain e.g. nottrusted.com. In the latter case, any subdomain of nottrusted.com can access the cookie. Loosely scoped cookies are common in mega-applications like google.com and live.com. Cookies set from a subdomain like app.foo.bar are transmitted only to that domain by the browser. However, cookies scoped to a parent-level domain may be transmitted to the parent, or any subdomain of the parent.

* URL: https://<proyecto>.supabase.co/rest/v1/equipments%3Fselect=id,name,category,status&order=name
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/equipments (order,select)`
  * Method: `GET`
  * Parameter: ``
  * Attack: ``
  * Evidence: `Domain=supabase.co`
  * Other Info: `The origin domain used for comparison was:
<proyecto>.supabase.co
Cookie name: __cf_bm
`
* URL: https://<proyecto>.supabase.co/rest/v1/loans%3Fselect=id,user_id,equipment_id,status,request_date,return_date,environment,purpose,duration_hours,reviewed_by,rejection_reason,latitude,longitude,solicitante:users!loans_user_id_fkey(full_name&29&order=request_date
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/loans (order,select)`
  * Method: `GET`
  * Parameter: ``
  * Attack: ``
  * Evidence: `Domain=supabase.co`
  * Other Info: `The origin domain used for comparison was:
<proyecto>.supabase.co
Cookie name: __cf_bm
`
* URL: https://<proyecto>.supabase.co/rest/v1/returns%3Fselect=id,loan_id,equipment_condition,notes,return_date,latitude,longitude
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/returns (select)`
  * Method: `GET`
  * Parameter: ``
  * Attack: ``
  * Evidence: `Domain=supabase.co`
  * Other Info: `The origin domain used for comparison was:
<proyecto>.supabase.co
Cookie name: __cf_bm
`
* URL: https://<proyecto>.supabase.co/rest/v1/rpc/iniciar_sesion
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/rpc/iniciar_sesion ()({p_identificador,p_hash})`
  * Method: `POST`
  * Parameter: ``
  * Attack: ``
  * Evidence: `Domain=supabase.co`
  * Other Info: `The origin domain used for comparison was:
<proyecto>.supabase.co
Cookie name: __cf_bm
`
* URL: https://<proyecto>.supabase.co/rest/v1/rpc/sesion_valida
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/rpc/sesion_valida ()({})`
  * Method: `POST`
  * Parameter: ``
  * Attack: ``
  * Evidence: `Domain=supabase.co`
  * Other Info: `The origin domain used for comparison was:
<proyecto>.supabase.co
Cookie name: __cf_bm
`

Instances: Systemic


### Solution

Always scope cookies to a FQDN (Fully Qualified Domain Name).

### Reference


* [ https://datatracker.ietf.org/doc/html/rfc6265#section-4.1 ](https://datatracker.ietf.org/doc/html/rfc6265#section-4.1)
* [ https://owasp.org/www-project-web-security-testing-guide/v41/4-Web_Application_Security_Testing/06-Session_Management_Testing/02-Testing_for_Cookies_Attributes.html ](https://owasp.org/www-project-web-security-testing-guide/v41/4-Web_Application_Security_Testing/06-Session_Management_Testing/02-Testing_for_Cookies_Attributes.html)
* [ https://code.google.com/archive/p/browsersec/wikis/Part2.wiki ](https://code.google.com/archive/p/browsersec/wikis/Part2.wiki)


#### CWE Id: [ 565 ](https://cwe.mitre.org/data/definitions/565.html)


#### WASC Id: 15

#### Source ID: 3

### [ Re-examine Cache-control Directives ](https://www.zaproxy.org/docs/alerts/10015/)



##### Informational (Low)

### Description

The cache-control header has not been set properly or is missing, allowing the browser and proxies to cache content. For static assets like css, js, or image files this might be intended, however, the resources should be reviewed to ensure that no sensitive content will be cached.

* URL: https://<proyecto>.supabase.co/rest/v1/activities%3Fselect=id,title,description,location,scheduled_at,instructor_id&order=scheduled_at
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/activities (order,select)`
  * Method: `GET`
  * Parameter: `cache-control`
  * Attack: ``
  * Evidence: ``
  * Other Info: ``
* URL: https://<proyecto>.supabase.co/rest/v1/equipments%3Fselect=id,name,category,status&order=name
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/equipments (order,select)`
  * Method: `GET`
  * Parameter: `cache-control`
  * Attack: ``
  * Evidence: ``
  * Other Info: ``
* URL: https://<proyecto>.supabase.co/rest/v1/loans%3Fselect=id,user_id,equipment_id,status,request_date,return_date,environment,purpose,duration_hours,reviewed_by,rejection_reason,latitude,longitude,solicitante:users!loans_user_id_fkey(full_name&29&order=request_date
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/loans (order,select)`
  * Method: `GET`
  * Parameter: `cache-control`
  * Attack: ``
  * Evidence: ``
  * Other Info: ``
* URL: https://<proyecto>.supabase.co/rest/v1/loans%3Fselect=id,user_id,equipment_id,status,request_date,return_date,environment,purpose,duration_hours,reviewed_by,rejection_reason,latitude,longitude,solicitante:users!loans_user_id_fkey(full_name&29&order=request_date&user_id=eq.9098a9df-41f6-45a5-91dd-e032ebd6892d
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/loans (order,select,user_id)`
  * Method: `GET`
  * Parameter: `cache-control`
  * Attack: ``
  * Evidence: ``
  * Other Info: ``
* URL: https://<proyecto>.supabase.co/rest/v1/returns%3Fselect=id,loan_id,equipment_condition,notes,return_date,latitude,longitude
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/returns (select)`
  * Method: `GET`
  * Parameter: `cache-control`
  * Attack: ``
  * Evidence: ``
  * Other Info: ``

Instances: Systemic


### Solution

For secure content, ensure the cache-control HTTP header is set with "no-cache, no-store, must-revalidate". If an asset should be cached consider setting the directives "public, max-age, immutable".

### Reference


* [ https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html#web-content-caching ](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html#web-content-caching)
* [ https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Cache-Control ](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Cache-Control)
* [ https://grayduck.mn/2021/09/13/cache-control-recommendations/ ](https://grayduck.mn/2021/09/13/cache-control-recommendations/)


#### CWE Id: [ 525 ](https://cwe.mitre.org/data/definitions/525.html)


#### WASC Id: 13

#### Source ID: 3

### [ Session Management Response Identified ](https://www.zaproxy.org/docs/alerts/10112/)



##### Informational (Medium)

### Description

The given response has been identified as containing a session management token. The 'Other Info' field contains a set of header tokens that can be used in the Header Based Session Management Method. If the request is in a context which has a Session Management Method set to "Auto-Detect" then this rule will change the session management to use the tokens identified.

* URL: https://<proyecto>.supabase.co/rest/v1/activities%3Fid=eq.0f9d2059-284d-4a33-a95b-db2aecabfead
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/activities (id)`
  * Method: `DELETE`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/equipments%3Fid=eq.692e2acc-d7a0-4eca-b195-aaa5dee74449
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/equipments (id)`
  * Method: `DELETE`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/activities%3Fselect=id,title,description,location,scheduled_at,instructor_id&order=scheduled_at
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/activities (order,select)`
  * Method: `GET`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/activities%3Fselect=id
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/activities (select)`
  * Method: `GET`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/equipments%3Fselect=id&id=eq.5ec00000-0000-4000-8000-00000000000e
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/equipments (id,select)`
  * Method: `GET`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/equipments%3Fselect=id,name,category,status&order=name
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/equipments (order,select)`
  * Method: `GET`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/equipments%3Fselect=id
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/equipments (select)`
  * Method: `GET`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/evidences%3Fselect=id
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/evidences (select)`
  * Method: `GET`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/loans%3Fselect=status,solicitante:users!loans_user_id_fkey(full_name&29&limit=1
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/loans (limit,select)`
  * Method: `GET`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/loans%3Fselect=id,user_id,equipment_id,status,request_date,return_date,environment,purpose,duration_hours,reviewed_by,rejection_reason,latitude,longitude,solicitante:users!loans_user_id_fkey(full_name&29&order=request_date
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/loans (order,select)`
  * Method: `GET`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/loans%3Fselect=id,user_id,equipment_id,status,request_date,return_date,environment,purpose,duration_hours,reviewed_by,rejection_reason,latitude,longitude,solicitante:users!loans_user_id_fkey(full_name&29&order=request_date&user_id=eq.9098a9df-41f6-45a5-91dd-e032ebd6892d
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/loans (order,select,user_id)`
  * Method: `GET`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/loans%3Fselect=id
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/loans (select)`
  * Method: `GET`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/returns%3Fselect=id,loan_id,equipment_condition,notes,return_date,latitude,longitude,loans!inner(user_id&29&loans.user_id=eq.9098a9df-41f6-45a5-91dd-e032ebd6892d
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/returns (loans.user_id,select)`
  * Method: `GET`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/returns%3Fselect=id,loan_id,equipment_condition,notes,return_date,latitude,longitude
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/returns (select)`
  * Method: `GET`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/sesiones%3Fselect=*
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/sesiones (select)`
  * Method: `GET`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/users%3Fselect=*
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/users (select)`
  * Method: `GET`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/equipments%3Fid=eq.0b1e0000-0000-4000-8000-000000000003
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/equipments (id)({name})`
  * Method: `PATCH`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/loans%3Fid=eq.5eed0000-0000-4000-8000-000000000001
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/loans (id)({reviewed_by})`
  * Method: `PATCH`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/loans%3Fid=eq.5eed0000-0000-4000-8000-000000000001
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/loans (id)({status})`
  * Method: `PATCH`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/activities
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/activities ()({id,title,description,location,scheduled_at,instructor_id})`
  * Method: `POST`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/equipments
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/equipments ()({id,name,title,category,status})`
  * Method: `POST`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/equipments
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/equipments ()({id,title,name,category,status})`
  * Method: `POST`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/loans
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/loans ()({id,user_id,user_role,equipment_id,status,request_date,return_date,environment,purpose,duration_hours})`
  * Method: `POST`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/rpc/cerrar_sesion
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/rpc/cerrar_sesion ()({})`
  * Method: `POST`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/rpc/iniciar_sesion
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/rpc/iniciar_sesion ()({p_identificador,p_hash})`
  * Method: `POST`
  * Parameter: `[0].token`
  * Attack: ``
  * Evidence: `[0].token`
  * Other Info: `json:[0].token
cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/rpc/iniciar_sesion
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/rpc/iniciar_sesion ()({p_identificador,p_hash})`
  * Method: `POST`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`
* URL: https://<proyecto>.supabase.co/rest/v1/rpc/sesion_valida
  * Node Name: `https://<proyecto>.supabase.co/rest/v1/rpc/sesion_valida ()({})`
  * Method: `POST`
  * Parameter: `__cf_bm`
  * Attack: ``
  * Evidence: `__cf_bm`
  * Other Info: `cookie:__cf_bm`


Instances: 27

### Solution

This is an informational alert rather than a vulnerability and so there is nothing to fix.

### Reference


* [ https://www.zaproxy.org/docs/desktop/addons/authentication-helper/session-mgmt-id/ ](https://www.zaproxy.org/docs/desktop/addons/authentication-helper/session-mgmt-id/)



#### Source ID: 3


