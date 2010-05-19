"""
 Copyright 2010 EDL FOUNDATION

 Licensed under the EUPL, Version 1.1 or as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 you may not use this work except in compliance with the
 Licence.
 You may obtain a copy of the Licence at:

 http://ec.europa.eu/idabc/eupl

 Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the Licence for the specific language governing
 permissions and limitations under the Licence.


 Created by: Jacob Lundqvist (Jacob.Lundqvist@gmail.com)

 Generic utility functions
"""

import hashlib

def dict_2_django_choice(d):
    lst = []
    for key in d:
        lst.append((key, d[key]))
    return lst


def calculate_hash(item):
    """
    When calculating the content hash for the record, the following is asumed:
      the lines are stripped for initial and trailing whitespaces,
      sorted alphabetically
      each line is separated by one \n character
      and finaly the <record> and </record> should be kept!
    """
    r_hash = hashlib.sha256(item.upper()).hexdigest().upper()
    return r_hash


def __db_is_mysql():
    from django.db import connection
    cursor = connection.cursor()
    if hasattr(cursor, 'db'):
        # if DEBUG=True its found here...
        look_at = cursor.db
    else:
        # Otherwise we find it here
        look_at = cursor
    if look_at.__str__().find('mysql') > -1:
        b = True
    else:
        b = False
    return b


if __name__ == "__main__":
    import sys
    if len(sys.argv) != 2:
        print 'Param should be the item that should be hashed'
        sys.exit(1)
    print calculate_hash(sys.argv[1])
else:
    db_is_mysql = __db_is_mysql()
