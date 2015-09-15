declare variable $db   external := 'test1';
declare variable $path external := '/';
declare variable $user external := 'admin';

db:replace($db, $path, fn:concat('<user>',$user,'</user>'))
