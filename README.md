# ciclismo

A Clojure library designed to:
1. Manage ciclist training
2. Manage competitive ciclists races
3. Manage competitive against the clock races

## Usage

1. Clone the repository
2. Create directory ciclismo/resources/private
3. Create a file "config.clj" in the above directory
   {:db-protocol "mysql"
    :db-name "//localhost:3306/[your database name]?characterEncoding=UTF-8"
    :db-user "[your database user here ex root]"
    :db-pwd "[your database password here]"
    :db-class "com.mysql.cj.jdbc.Driver"
    :email-host "[your email smtp server here ex smtp.gmail.com]"
    :email-user "[your email user here ex user@gmail.com]"
    :email-password "[your email password here]"
    :port 3000
    :tz "US/Pacific"
    :site-name "Site Name"
    :base-url "http://0.0.0.0:3000/"
    :uploads "./uploads"
    :path "/uploads/"}
4. Create a mysql database. You can find all the tables an how to create them in: ciclismo/src/ciclismo/models/cdb.clj
## License

Copyright © 2019 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
