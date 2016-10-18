(:~ Path to resource. :)
declare variable $PATH as xs:string external;
(:~ New path to resource. :)
declare variable $NEWPATH as xs:string external;

let $db := if(contains($PATH, '/')) then substring-before($PATH, '/') else $PATH
let $path := substring-after($PATH, '/')
let $newpath := substring-after($NEWPATH, '/')

let $is-db := (string-length($path) = 0)

let $histdb := '~history_'
let $metadb := '~meta_'
let $oldmetadb := concat($metadb, $db)
let $oldhistdb := concat($histdb, $db)
let $newmetadb := concat($metadb, $NEWPATH)
let $newhistdb := concat($histdb, $NEWPATH)
let $metapath := concat($path, '.xml')
let $newmetapath := concat($newpath, '.xml')

(: Check for admin rights. :)
let $user := user:current()
let $userdetails :=  try {
	user:list-details($user)
} catch * {
	()
}
let $permission := if (empty($userdetails)) then (
	'not'
) else (
	$userdetails/@permission/string()
)
let $is-admin := ($permission eq 'admin')

return if($is-db) then (
    if ($is-admin) then (
        (: rename database :)
        db:alter($db, $NEWPATH),
        db:alter($oldmetadb, $newmetadb),
        db:alter($oldhistdb, $newhistdb)
    ) else (
        error(xs:QName("api"), "Need admin rights to rename databases")
    )
) else (
    db:rename($db, $path, $newpath),
    db:rename($oldmetadb, $metapath, $newmetapath)
)