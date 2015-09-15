declare variable $db   external := 'test1';
declare variable $path external := '/';
declare variable $user external := 'admin';

if (db:exists($db, $path)) then
    not(ft:contains(db:open($db, $path), $user))
else db:exists($db, $path)