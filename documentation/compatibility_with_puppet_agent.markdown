---
layout: default
title: "Puppet Server: Backward Compatibility with Puppet 3 Agents"
canonical: "/puppetserver/latest/compatibility_with_puppet_agent.html"
---

[ca.conf]: ./config_file_ca.markdown
[auth.conf]: https://puppet.com/docs/puppet/latest/config_file_auth.html
[future parser]: https://docs.puppet.com/puppet/3.8/experiments_future.html
[upgrade puppet]: https://puppet.com/docs/puppet/4.10/upgrade_major_pre.html
[deprecated]: https://puppet.com/docs/puppetserver/2.7/deprecated_features.html
[Puppet Server `auth.conf` documentation]: ./config_file_auth.markdown

In version 2.1 and later, Puppet Server can serve configurations to both Puppet 4 and Puppet 3 agents. After your Puppet 3 nodes work with a newer Puppet Server, start upgrading them to Puppet 4.

## Preparing Puppet 3 Nodes for Puppet Server 2

Backward compatibility is enabled by default, but you should ensure your Puppet code is ready for Puppet 4 before pointing old agents at a new Puppet server. You might also need to edit `auth.conf`.

Before migrating Puppet 3 nodes to Puppet Server 2, do all of the following:

* Update Puppet to the most recent Puppet 3 version on your agents and masters.
* Set `stringify_facts = false` on all nodes, and fix your code if necessary.
* Set `parser = future` on your Puppet masters, and fix your code if necessary.
* Modify your custom `auth.conf` rules for the new Puppet Server version.

### Update Puppet 3

Follow the steps in [the Puppet documentation][upgrade Puppet] to prepare your Puppet 3 deployment for a major version upgrade. This is especially important on your Puppet master, where you'll want the newest version of the [future parser][].

### Transfer and Update `auth.conf`

Puppet 3 and 4 use different HTTPS URLs to fetch configurations. Puppet Server lets agents make requests at the old URLs, but internally it handles them as requests to the new endpoints. Any rules in `auth.conf` that match Puppet 3-style URLs will have _no effect._

This means you must:

* Check any _custom_ rules you've added to your `auth.conf` file. (Don't worry about default rules.)

    You must reimplement your authorization rules in `/etc/puppetlabs/puppetserver/conf.d/auth.conf` and use the configuration file's new HOCON format. See the [Puppet Server `auth.conf` documentation][] for more information.

For more information, see:

* [Puppet 4 HTTPS API documentation](https://puppet.com/docs/puppet/latest/http_api/http_api_index.html)
* [Puppet 3 HTTPS API documentation](https://docs.puppet.com/references/3.8.0/developer/file.http_api_index.html)
* [Puppet 4 `auth.conf` documentation][auth.conf]
* [Puppet 3 `auth.conf` documentation](https://docs.puppet.com/puppet/3.8/config_file_auth.html)

#### Example `auth.conf` Rules for Puppet 3 and 4 Agents

The examples in this section convert this Puppet 3 example `auth.conf` rule so that it is compatible with Puppet 4:

To support both Puppet 3 and Puppet 4 agents, modify the rules to follow the new HOCON `auth.conf` format and place the new rules in `/etc/puppetlabs/puppetserver/conf.d/auth.conf`:

~~~
authorization: {
    version: 1
    rules: [
        ...
        {
            # Puppet 3 & 4 compatible auth.conf with Puppet Server 2.2+
            match-request: {
                path: "^/puppet/v3/catalog/([^/]+).uuid$"
                type: regex
                method: [get, post]
            }
            allow: "/^$1|.uuid.*/"
            sort-order: 200
            name: "my catalog"
        },
        {
            # Default rule, should follow the more specific rules
            match-request: {
                path: "^/puppet/v3/catalog/([^/]+)$"
                type: regex
                method: [get, post]
            }
            allow: "$1"
            sort-order: 500
            name: "puppetlabs catalog"
        },
        ...
    ]
}
~~~

> **Note:** For more detailed `auth.conf` conversion examples, see [Migrating to the HOCON auth.conf Format](./config_file_auth_migration.markdown).

## Details About Backward Compatibility

The `legacy-routes-service` intercepts Puppet 3 HTTPS API requests, transforms the URLs and request headers into Puppet 4 compatible requests, and passes them to the Puppet master service.

### HTTP Headers

There are some minor differences in headers between native Puppet 4 requests and modified Puppet 3 requests. Specifically:

* The `X-Puppet-Version` header is absent in Puppet 3 requests. This should only matter if you use this header to configure a third-party reverse proxy like HAProxy or NGINX.
* The `Accept:` request header is changed to match the content the Puppet 4 master service can provide. For example, `Accept: raw` and `Accept: s` are changed to `Accept: binary`.

    This header is used, albeit rarely, to configure reverse proxies.
* The `content-type` for file bucket content requests is treated internally as `application/octet-stream`, but the request and the response are treated as `text/plain` for compatibility with Puppet 3.
