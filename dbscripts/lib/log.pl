#!/usr/bin/perl

#
#	Logfile Module (3.6.1998)	
#
# 	USAGE: tie *HANDLE, 'Logfile', '<filename>', '<modulename';
#
# 	$VER: 1.1$
#

package Logfile;

$LOCK_EX=2;
$LOCK_UN=8;

sub mkname {
	my($name)=shift;
	my @timestr=localtime(time);
	
	$name=~s/\%d/$timestr[3]/eg;
	$name=~s/\%m/$timestr[4]+1/eg;
	$name=~s/\%y/$timestr[5]+1900/eg;
	
	return $name;
	}

# no reading from logfile
sub READLINE { return undef; }	
sub READ { return undef; }
sub GETC { return undef; }

sub PRINT {
	my $obj=shift;
	my @text=@_;
	my @timestr=localtime(time);
	
	local(*OUT);
	
	if ((scalar(@text)>1) && ($text[0]=~/^\d+$/)) {
		my($level)=shift(@text);
		return if ($level<$$obj{'loglevel'});
		}
		
	if (open(OUT,">>".$$obj{'filename'})) {
		flock(OUT,$LOCK_EX);
		seek(OUT,0,2);
		printf OUT ("%02d.%02d.%s %02d:%02d:%02d [%d] %s: %s",
                ($timestr[3]),
                ($timestr[4]+1),
                ($timestr[5]+1900),
                $timestr[2],$timestr[1],$timestr[0],
                $$,$$obj{'module'},
                join("",@text));
		flock(OUT,$LOCK_UN);
		close(OUT);
		}
	}
	
sub TIEHANDLE {
	my $obj;
	shift;
	$$obj{'filename'}=&mkname(shift);
	$$obj{'module'}=shift;
	$$obj{'loglevel'}=(shift || 0);
	 
	bless $obj, 'Logfile';
	
	return $obj;
	}	
1;
