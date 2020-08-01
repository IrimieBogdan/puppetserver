---
layout: default
built_from_commit: 5bfb65354358d6544a36b0195b4d703708a4123d
title: 'Puppet HTTP API: Certificate Status'
canonical: "/puppet/latest/http_api/http_certificate_status.html"
---

Certificate Status
===============

The `certificate status` endpoint allows a client to read or alter the
status of a certificate or pending certificate request. It is only
useful on the CA.

Find
----

    GET /puppet-ca/v1/certificate_status/:certname
    Accept: application/json, text/pson

Retrieve information about the specified certificate. Similar to `puppetserver ca list --certname <certname>`.

Search
-----

    GET /puppet-ca/v1/certificate_statuses/:any_key?state=:state
    Accept: application/json, text/pson

Retrieve information about all known certificates. Similar to `puppetserver ca list --all`. A key is required but is ignored.

### Parameters

* `state` (optional): The certificate state by which to filter search results. Valid states are 'requested', 'signed', and 'revoked'.

Save
----

    PUT /puppet-ca/v1/certificate_status/:certname
    Content-Type: text/pson

Change the status of the specified certificate. The desired state
is sent in the body of the PUT request as a one-item PSON hash; the two
allowed complete hashes are:

* `{"desired_state":"signed"}` (for signing a certificate signing request, similar to `puppetserver ca sign`). To set the validity period of the signed certificate, specify the `cert_ttl` key in the body of the request, with an integer value. By default, this key specifies the number of seconds, but you can specify another time unit. See [configuration](https://puppet.com/docs/puppet/latest/configuration.html#configuration-settings) for a list of Puppet's accepted time unit markers.
* `{"desired_state":"revoked"}` (for revoking a certificate, similar to
`puppetserver ca revoke`).

Note that revoking a certificate does not clean up other info about the
host --- see the DELETE request for more information.

Delete
-----

    DELETE /puppet-ca/v1/certificate_status/:hostname
    Accept: application/json, text/pson

Cause the certificate authority to discard all SSL information regarding
a host (including any certificates, certificate requests, and keys).
This does not revoke the certificate if one is present; if you wish to
emulate the behavior of `puppet cert --clean`, you must PUT a
`desired_state` of `revoked` before deleting the host’s SSL information.

If the deletion was successful, it returns a string listing the deleted
classes like

    "Deleted for myhost: Puppet::SSL::Certificate, Puppet::SSL::Key"

Otherwise it returns

    "Nothing was deleted"

### Supported HTTP Methods

This endpoint is disabled in the default configuration. It is
recommended to be careful with this endpoint, as it can allow control
over the certificates used by the puppet master.

GET, PUT, DELETE


### Supported Response Formats

`application/json`, `text/pson`, `pson`

This endpoint can produce yaml as well, but the returned data is
incomplete.

### Examples

#### Certificate information

    GET /puppet-ca/v1/certificate_status/mycertname

    HTTP/1.1 200 OK
    Content-Type: text/pson

    {
      "name":"mycertname",
      "state":"signed",
      "fingerprint":"A6:44:08:A6:38:62:88:5B:32:97:20:49:8A:4A:4A:AD:65:C3:3E:A2:4C:30:72:73:02:C5:F3:D4:0E:B7:FC:2F",
      "fingerprints":{
        "default":"A6:44:08:A6:38:62:88:5B:32:97:20:49:8A:4A:4A:AD:65:C3:3E:A2:4C:30:72:73:02:C5:F3:D4:0E:B7:FC:2F",
        "SHA1":"77:E6:5A:7E:DD:83:78:DC:F8:51:E3:8B:12:71:F4:57:F1:C2:34:AE",
        "SHA256":"A6:44:08:A6:38:62:88:5B:32:97:20:49:8A:4A:4A:AD:65:C3:3E:A2:4C:30:72:73:02:C5:F3:D4:0E:B7:FC:2F",
        "SHA512":"CA:A0:8C:B9:FE:9D:C2:72:18:57:08:E9:4B:11:B7:BC:4E:F7:52:C8:9C:76:03:45:B4:B6:C5:D2:DC:E8:79:43:D7:71:1F:5C:97:FA:B2:F3:ED:AE:19:BD:A9:3B:DB:9F:A5:B4:8D:57:3F:40:34:29:50:AA:AA:0A:93:D8:D7:54"
      },
      "dns_alt_names":["DNS:puppet","DNS:mycertname"]
    }

#### Search unsigned certs (CSRs)

    GET /puppet-ca/v1/certificate_statuses/ignored?state=requested

    HTTP/1.1 200 OK
    Content-Type: text/pson

    [
        {
          "name":"mycertname1",
          "state":"requested",
          "fingerprint":"A6:44:08:A6:38:62:88:5B:32:97:20:49:8A:4A:4A:AD:65:C3:3E:A2:4C:30:72:73:02:C5:F3:D4:0E:B7:FC:2F",
          "fingerprints":{
            "default":"A6:44:08:A6:38:62:88:5B:32:97:20:49:8A:4A:4A:AD:65:C3:3E:A2:4C:30:72:73:02:C5:F3:D4:0E:B7:FC:2F",
            "SHA1":"77:E6:5A:7E:DD:83:78:DC:F8:51:E3:8B:12:71:F4:57:F1:C2:34:AE",
            "SHA256":"A6:44:08:A6:38:62:88:5B:32:97:20:49:8A:4A:4A:AD:65:C3:3E:A2:4C:30:72:73:02:C5:F3:D4:0E:B7:FC:2F",
            "SHA512":"CA:A0:8C:B9:FE:9D:C2:72:18:57:08:E9:4B:11:B7:BC:4E:F7:52:C8:9C:76:03:45:B4:B6:C5:D2:DC:E8:79:43:D7:71:1F:5C:97:FA:B2:F3:ED:AE:19:BD:A9:3B:DB:9F:A5:B4:8D:57:3F:40:34:29:50:AA:AA:0A:93:D8:D7:54"
          },
          "dns_alt_names":[]
        },
        {
          "name":"mycertname2",
          "state":"requested",
          "fingerprint":"A6:44:08:A6:38:62:88:5B:32:97:20:49:8A:4A:4A:AD:65:C3:3E:A2:4C:30:72:73:02:C5:F3:D4:0E:B7:FC:2F",
          "fingerprints":{
            "default":"A6:44:08:A6:38:62:88:5B:32:97:20:49:8A:4A:4A:AD:65:C3:3E:A2:4C:30:72:73:02:C5:F3:D4:0E:B7:FC:2F",
            "SHA1":"77:E6:5A:7E:DD:83:78:DC:F8:51:E3:8B:12:71:F4:57:F1:C2:34:AE",
            "SHA256":"A6:44:08:A6:38:62:88:5B:32:97:20:49:8A:4A:4A:AD:65:C3:3E:A2:4C:30:72:73:02:C5:F3:D4:0E:B7:FC:2F",
            "SHA512":"CA:A0:8C:B9:FE:9D:C2:72:18:57:08:E9:4B:11:B7:BC:4E:F7:52:C8:9C:76:03:45:B4:B6:C5:D2:DC:E8:79:43:D7:71:1F:5C:97:FA:B2:F3:ED:AE:19:BD:A9:3B:DB:9F:A5:B4:8D:57:3F:40:34:29:50:AA:AA:0A:93:D8:D7:54"
          },
          "dns_alt_names":[]
        }
    ]

#### Revoking a certificate

    PUT /puppet-ca/v1/certificate_status/mycertname HTTP/1.1
    Content-Type: text/pson
    Content-Length: 27

    {"desired_state":"revoked"}

This has no meaningful return value.


#### Deleting the certificate information

    DELETE /puppet-ca/v1/certificate_status/mycertname HTTP/1.1

Gets the response:

    "Deleted for mycertname: Puppet::SSL::Certificate, Puppet::SSL::Key"

Schema
-----

Find and search operations return objects which
conform to [the host schema.](../schemas/host.json)
