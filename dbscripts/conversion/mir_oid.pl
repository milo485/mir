#!/usr/bin/perl -w
use Pg;

#if (!defined($ARGV[0])) {
#        print "Usage: $0 name_of_filelist_file\n";
#        exit 1;
#}

#$dump="$ARGV[0]";

my $conn = Pg::connectdb("dbname=indy3 user=postgres 
host=localhost");
( PGRES_CONNECTION_OK eq $conn->status )
    and print "Pg::connectdb ........... ok\n"
    or  die   "Pg::connectdb ........... not ok: ", $conn->errorMessage;


    #open(DUMP,"$dump") || die "can't open dump file $dump";

    #$basedir="/cdrom";

Pg::doQuery($conn, "select content_data from content", \@ary);

for $i ( 0 .. $#ary ) {
    for $j ( 0 .. $#{$ary[$i]} ) {
        print "$ary[$i][$j]\t";
    }
    print "\n";
}

#while(<DUMP>) {
#        chomp;
#        next if (/^$/ || /^\s*$/);
#
#		# exception liste einlesen...
#
#
#				# hier insert:
#
#				$conn->exec("BEGIN");
#				$oid= $conn->lo_import("$filename");
#				print $conn->errorMessage."\n";
#				print "trying to insert icon\n";
#				$ioid= $conn->lo_import("/tmp/iconblob.jpg");
#				print $conn->errorMessage."\n";
#				$sql="INSERT INTO images 
#(title,date,place,author,to_img_layout,to_img_type,is_classified,to_media_f
#older,to_img_color,comment,webdb_create,img_width,img_height,image_data,ico
#n_data) ".
#					 "VALUES ('".$titel."','20010108','".$ort.
#					 "','Hoch die Kampf 
#dem','".$layout."','1','0','2','2','".$comment."',now(),'".$img_width."','"
#.$img_height.
#					 "','".$oid."','".$ioid."')";
#				print $sql."\n";
#				$conn->exec( $sql );
#				print $conn->errorMessage."\n";
#				$conn->exec("END");
#
#
#
#}
## end of while
#



