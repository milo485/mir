#!/usr/bin/perl

#
# wizard
#

use DBI;

$0=~m#[^/]+$#;
$cmdpath=$`;
if ($cmdpath eq "") { $cmdpath="./"; }
unshift(@INC,$cmdpath."lib");

require "codec.pl";
require "subst.pl";

sub USAGE {
	print "USAGE: wizard.pl dsn user passwd table dir\n";
	exit(0);
	}

$dsn=shift(@ARGV) || &USAGE;
if (($dsn eq "-h") || ($dsn eq "--help")) { &USAGE; }
$user=shift(@ARGV) || &USAGE;
$passwd=shift(@ARGV) || &USAGE;
if ($passwd eq "-") { $passwd=""; }
$table=shift(@ARGV) || &USAGE;
$dir=shift(@ARGV) || &USAGE;

$dbh=DBI->connect($dsn,$user,$passwd);
if (!defined $dsn) {
	print "Error: cannot connect $dsn\n";
	&USAGE;
	}

$tables=$dbh->table_info;
while ($hash=$tables->fetchrow_hashref) {
	#print $$hash{'TABLE_NAME'}."\n";

	if ($$hash{'TABLE_NAME'} eq $table) {

		$sth=$dbh->prepare("show fields from ".$$hash{'TABLE_NAME'});
		$sth->execute;
		while ($fieldhash=$sth->fetchrow_hashref) {
			#print "\t".$$fieldhash{'Field'}.":".$$fieldhash{'Type'}."\n";
			push(@data,[$$fieldhash{'Field'},$$fieldhash{'Type'}]);
			}
		$sth->finish;	
		}
	}

$$env{'fields'}{'_data'}=\@data;
$$env{'fields'}{'_pos'}=0;
$$env{'fields'}{'_size'}=scalar(@data);

$$env{'tablename'}=$table;

print "prosessing files in $dir\n";

chdir $dir;

if (opendir(DIR,".")) {
	while ($file=readdir(DIR)) {
		next unless (-f $file);
		if ($file=~/\.template$/) {
			$newfile=$`;
			print "\t$file -> $newfile\n";
			if (open(IN,$file)) {
				if (open(OUT,">$newfile")) {
					&Subst::process(*IN,*OUT,$env);
					close(OUT);
					}
				close(IN);	
				}
			}
		}
	closedir(DIR);
	}			



	

