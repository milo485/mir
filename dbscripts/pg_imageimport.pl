#!/usr/local/bin/perl -w
use Pg;

if (!defined($ARGV[0])) {
        print "Usage: $0 name_of_filelist_file\n";
        exit 1;
}

$dump="$ARGV[0]";

my $conn = Pg::connectdb("dbname=indy user=indy host=192.168.4.8");
( PGRES_CONNECTION_OK eq $conn->status )
    and print "Pg::connectdb ........... ok\n"
    or  die   "Pg::connectdb ........... not ok: ", $conn->errorMessage;


open(DUMP,"$dump") || die "can't open dump file $dump";

$basedir="";

while(<DUMP>) {
        chomp;
        next if (/^$/ || /^\s*$/);

		# exception liste einlesen...
				$filename=$basedir.$_;
				$comment=$filename;
				$titel="unklassifiziert";
				$ort="unklassifiziert"; 
			
				$_ = `/usr/X11R6/bin/identify $filename`;
				/(\d+)x(\d+)/;
				$img_width=$1;
				$img_height=$2;
				
				$layout=0; 						# undefined
				if ( $img_width>$img_height ) {$layout=1;}		# querformat
				if ( $img_width<$img_height ) {$layout=2;}		# hochformat
				if ( $img_width==$img_height) {$layout=3;}    # quadratisch

				# hier insert:
				print "making icon: $filename \n";
				`cp $filename /tmp/iconblob.jpg`;
				`/usr/X11R6/bin/mogrify -geometry 120x120 /tmp/iconblob.jpg`;
				
				print "trying to insert: $filename \n";
				
				$conn->exec("BEGIN");
				$oid= $conn->lo_import("$filename");
				print $conn->errorMessage."\n";
				print "trying to insert icon\n";
				$ioid= $conn->lo_import("/tmp/iconblob.jpg");
				print $conn->errorMessage."\n";
				$sql="INSERT INTO images (title,date,place,author,to_img_layout,to_img_type,is_classified,to_media_folder,to_img_color,comment,webdb_create,img_width,img_height,image_data,icon_data) ".
					 "VALUES ('".$titel."','20010109','".$ort.
					 "','huh','".$layout."','1','0','3','2','".$comment."',now(),'".$img_width."','".$img_height.
					 "','".$oid."','".$ioid."')";
				print $sql."\n";
				$conn->exec( $sql );
				print $conn->errorMessage."\n";
				$conn->exec("END");
	
			

}
# end of while
