declare variable $db   external := 'test1';
declare variable $path external := '/';
db:replace($db, $path, <a/>)