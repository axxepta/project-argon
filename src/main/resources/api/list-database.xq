(:~ Path to resource. :)
declare variable $PATH as xs:string external;

if(string-length($PATH) = 0) then (
(: skip locking databases :)
for $db in db:list()[not(starts-with(., '~'))]
return ('directory', $db)
) else (
(: name of database :)
let $db := if(contains($PATH, '/')) then substring-before($PATH, '/') else $PATH
(: path: ensure existence trailing slash :)
let $path := replace(substring-after($PATH, '/'), '([^/])$', '$1/')
(: retrieve all entries on this and lower levels :)
let $resources := db:list-details($db, $path)
(: retrieve entries of current level :)
for $entry in distinct-values(
        for $resource in $resources
        let $without-root := substring($resource, string-length($path) + 1)
        let $name := substring-before($without-root, '/')
        return if($name) then (
            $name
        ) else (
            $without-root
        )
)
let $full-path := $path || $entry
let $resource := $resources[. = $full-path]
let $dir := empty($resource)
(: show directories first, case-insensitive order :)
order by $dir descending, lower-case($entry)
return if($dir) then (
    'directory', $entry
) else (
(: xml and binary resources :)
'resource', $entry
)
)