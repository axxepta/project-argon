(:~ Database name. :)
declare variable $DB as xs:string external;
(:~ Path to resource. :)
declare variable $PATH as xs:string external;
(:~ New path to resource. :)
declare variable $NEWPATH as xs:string external;

db:rename($DB, $PATH, $NEWPATH)