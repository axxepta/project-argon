(:~ Path to resource. :)
declare variable $PATH as xs:string external;
(:~ New path to resource. :)
declare variable $NEWPATH as xs:string external;

let $db := if(contains($PATH, '/')) then substring-before($PATH, '/') else $PATH
let $path := substring-after($PATH, '/')
let $newpath := substring-after($NEWPATH, '/')

return if(string-length($path) = 0) then (
(: rename database :)
db:alter($db, $NEWPATH)
) else (
db:rename($db, $path, $newpath)
)