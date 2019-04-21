# ciclismo

1. Manage ciclist training
2. Manage competitive ciclists races
3. Manage competitive against the clock races

## Installation

1. Clone the repository
2. Create directory resources/private
3. Create a new config file ex: resources/private/config.clj
4. Example of resources/private/config.clj 
  ```
  {:db-protocol    "mysql"
  :db-name        "//localhost:3306/cc?characterEncoding=UTF-8"
  :db-user        "root"
  :db-pwd         "xxxxxxxx"
  :db-class       "com.mysql.cj.jdbc.Driver"
  :email-host     "smtp.gmail.com"
  :email-user     "user@gmail.com"
  :email-password "xxxxxxxx"
  :port           3000
  :tz             "US/Pacific"
  :site-name      "Ciclismo Mexicali"
  :base-url       "http://0.0.0.0:3000/"
  :uploads        "./uploads"
  :path           "/uploads/"}
  ```
4. Create a mysql database. You can find all the tables an how to create them in: 

   src/ciclismo/models/cdb.clj
## Prerequesites
1. leiningen 2.0.0 or above installed
2. jdk8 or above
3. mySQL or MariaDB
## Running
Go to project directory:

Lein Run
## Demo
http://lucero-systems.cf
## Questions
hectorqlucero@gmail.com
## License

Copyright © 2019 LS

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
