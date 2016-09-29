(:~ Source. :)
declare variable $SOURCE as xs:string external;
(:~ Path to resource. :)
declare variable $PATH as xs:string external;

(:~ Lock database. :)
declare variable $LOCK-DB := '~argon';
declare variable $USER-FILE := '~usermanagement';

let $user := user:current()
let $lock-file := db:open($LOCK-DB, $USER-FILE)
let $my-lock := if(exists($lock-file)) then (
    $lock-file//*[name() = $SOURCE][text() = $PATH]
) else ()

let $locks := (
    if(exists($lock-file))
    then $lock-file
    else document { <usermanagement><locks/><groups><group name="admin"><user>admin</user></group></groups>
    <users><name>admin</name></users></usermanagement> }
) update (
insert node element { $SOURCE } { attribute user { $user },  $PATH } into .//locks )

return if(exists($lock-file) and exists($my-lock) ) then (
    ()
) else if(exists($lock-file) ) then (
    db:replace($LOCK-DB, $USER-FILE, $locks)
) else (
    db:add($LOCK-DB, $locks, $USER-FILE)
)