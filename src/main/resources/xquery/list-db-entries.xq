(:~
 : This query returns all database entries (directories and resources)
 : for a specified path.
 :)

(:~ Name of database. :)
declare variable $db   external := 'test1';
(:~ Database path. :)
declare variable $path external := '/';

element resources {
  (: normalize path: no leading slash, but trailing slash :)
  let $root := replace(replace($path,'^/', ''), '([^/])$', '$1/')
  (: retrieve all entries on this and lower levels :)
  let $resources := db:list-details($db, $root)
  (: retrieve entries of current level :)
  for $entry in distinct-values(
    for $resource in $resources
    let $without-root := substring($resource, string-length($root) + 1)
    let $name := substring-before($without-root, '/')
    return if($name) then (
      $name
    ) else (
      $without-root
    )
  )
  let $full-path := $root || $entry
  let $resource := $resources[. = $full-path]
  let $dir := empty($resource)
  (: show directories first, case-insensitive order :)
  order by $dir descending, $entry
  return if($dir) then (
    element directory {
      $entry
    }
  ) else if(xs:boolean($resource/@raw)) then (
    (: binary files :)
    element binary {
      attribute size {
        bin:length(db:retrieve($db, $full-path))
      },
      $entry
    }
  ) else (
    (: xml documents :)
    element document {
      attribute nodes {
        count(db:open($db, $full-path)//node())
      },
      $entry
    }
  )
}