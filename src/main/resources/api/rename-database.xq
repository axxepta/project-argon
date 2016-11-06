(:~ Path to resource. :)
declare variable $PATH as xs:string external;
(:~ New path to resource. :)
declare variable $NEWPATH as xs:string external;

let $db := if(contains($PATH, '/')) then substring-before($PATH, '/') else $PATH
let $path := substring-after($PATH, '/')
let $newpath := substring-after($NEWPATH, '/')

let $is-db := (string-length($path) = 0)
let $is-dir := if ($is-db) then (false) else (
    not(db:exists($db, $path))
)

let $histdb := '~history_'
let $metadb := '~meta_'
let $oldmetadb := concat($metadb, $db)
let $oldhistdb := concat($histdb, $db)
let $newmetadb := concat($metadb, $NEWPATH)
let $newhistdb := concat($histdb, $NEWPATH)
let $metapath := concat($path, '.xml')
let $newmetapath := concat($newpath, '.xml')
(: obtain all files if directory :)
let $files := if ($is-dir) then (
    db:list($db, $path)
) else ( () )
let $newfiles := if ($is-dir) then (
    for $file in $files
    return concat($newpath, substring-after($file, $path))
) else ( () )
let $oldmetafiles := if ($is-dir) then (
    for $file in $files
    return concat($file, '.xml')
) else ( () )
let $newmetafiles := if ($is-dir) then (
    for $file in $newfiles
    return concat($file, '.xml')
) else ( () )

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
) else if($is-dir) then (
    for $file at $pos in $files
    return (
        db:rename($db, $file, $newfiles[position() = $pos]),
        db:rename($oldmetadb, $oldmetafiles[position() = $pos], $newmetafiles[position() = $pos])
    )
) else (
    db:rename($db, $path, $newpath),
    db:rename($oldmetadb, $metapath, $newmetapath)
)