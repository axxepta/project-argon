declare variable $db   external := 'test1';
declare variable $path external := '/';
declare variable $content external := '<a/>';

db:replace($db, $path, $content)
