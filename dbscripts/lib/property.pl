#!/usr/bin/perl

###########################################################
### property module
###
### $VER: 2.1$ 

package Property;

sub compile {
	my ($value)=shift;
	$value=~s/\$\((\w+)\)/$ENV{$1}/e;
	return $value;
	}

sub append {
	my($hash,$file)=@_;
	local(*FH);
	my($line,$cat,$slot,$key,$value,$from,$to);
	
	$cat="public";
	if (open(FH,$file)) {
		while (<FH>) {
			s/[\r\n]+//g;
			next if /^\s*$/;
			next if /^#/;
		
			if (/^\[(\w+)\]/) {
				$cat=$1;
				$$hash{$cat}{'_'}=$cat;
				next;
				}
			if (/^\[(\w+)\]\s*=\>\s*\[(\w+)\]$/) {
				$$hash{$2}=$$hash{$1};
				next;
				}
			if (/([^=\s]+)\s*=\s*(.*?)\s*$/) {
				$$hash{$cat}{$1}=$2;	
				next;
				}
			if (/([^=\s]+)\s*:\s*(.*)/) {
				$key=$1; $value=$2;
				$value=&compile($value);
				$$hash{$cat}{$key}=$value;	
				next;
				}
			if (/([^\<\s]+)\s*\<\<\s*(.*)/) {
				$slot=$1;
				undef @collect;
				while (<FH>) {
					last if substr($_,0,2) eq "<<";
					push(@collect,$_);
					}
				$$hash{$cat}{$slot}=join("\n",@collect);	
				}
			}
		close(FH);
		}
	else { print "CANNOT OPEN $file --- $!\n"; }
	}
	
sub read { 
	my($file)=shift;
	my($hash)={}; 
	
	&append($hash,$file); 
	return $hash; 
	}	

1;	
