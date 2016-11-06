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
let $resources := db:list-details($db, $path)/text()
(: extract directories on all levels :)
let $directories := distinct-values(
	for $resource in $resources
		let $pathtokens := tokenize($resource, '/')
		let $ntokens := count($pathtokens) - 1
		return if ($ntokens > 1) then (
			for $i in (2 to $ntokens)
				let $subpathsequence := subsequence($pathtokens, 1, $i)
				return string-join(
					for $component in $subpathsequence
						return ($component, '/')
				,'')
		) else (
			()
		)
)
(: combine and sort files and directories :)
let $allresources := sort(($resources, $directories))
(: return types and names, pair-wise :)
for $entry in $allresources
	return if(ends-with($entry, '/')) then (
		'directory', $entry
	) else (
		(: xml and binary resources :)
		'resource', $entry
	)
)