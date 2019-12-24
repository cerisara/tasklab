Pour s'auth avec REST vers le server Zimbra:

```
// Authentication part -------------------
String userpass = email_adress + ":" + password;
conn.setRequestProperty("X-Requested-With", "Curl");
String basicAuth = "Basic " + new String(android.util.Base64.encode(userpass.getBytes(), android.util.Base64.DEFAULT));
conn.setRequestProperty("Authorization", basicAuth);
// ----------------------------------------
```

get calendar avec curl:

```
curl --user 'cerisara:password' 'https://zimbra.inria.fr/home/cerisara/calendar?fmt=json&auth=ba&start=30day'
python -m json.tool < cal.json 
```

