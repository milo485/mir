#!/usr/bin/perl

################################################################
###	codec
###
### $VER: 2.1 (8bit,quoted-printable,perlish)$
### USAGE: $codec=&Codec::new('perlish');
###		 $encoded=$codec->encode($clear);
###		 $clear=$codec->decode($encoded);	

package Codec;


$VERSION="1.1";

sub new {
	my($type)=shift;
	my($res);
	
	return undef unless (defined $encode{$type});
	
	$$res{'encode'}=$encode{$type};
	$$res{'decode'}=$decode{$type};
	$$res{'type'}=$type;
		
	bless $res, "Codec";
	
	return $res;
	}

sub reset {
	}

sub encode {
	my($obj,$str)=@_;
	return &{$$obj{'encode'}}($str);
	}
	
sub decode {	
	my($obj,$str)=@_;
	return &{$$obj{'decode'}}($str);
	}

sub name {
	my($obj)=shift;
	return $$obj{'type'};
	}
	
################################################################
###	local

$crlf=pack("CC",13,10);

################################################################
###	id

$encode{'8bit'}=sub { return $_[0]; };
$decode{'8bit'}=sub { return $_[0]; };

################################################################
###	perlish

sub quote_perlish {
	my($str)=@_;
	
	$str=~s/\\/\\\\/g;
	
	$str=~s/\"/\\042/g;
	$str=~s/\'/\\047/g;

	$str=~s/\@/\\@/g;
	$str=~s/([\x80-\xFF])/sprintf("\\x%02x",ord($1))/eg;
	$str=~s/([\x00-\x20])/sprintf("\\x%02x",ord($1))/eg;
	return $str;
	}

sub convert_perlish {
	my($item)=shift;
	my($type,$key,$value,@res,$entry);
	
	$type=ref($item);
	
	if ((!defined $type)||($type eq "")) {
		return "\042".&quote_perlish($item)."\042";
		}
	else {
		if ($type eq "HASH") {
			while (($key,$value)=each %$item) {
				$entry="\042".&quote_perlish($key)."\042";
				$entry.="=>";
				$entry.=&convert_perlish($value);
				push(@res,$entry);
				}
			return "{".join(",",@res)."}";	
			}
		if ($type eq "ARRAY") {
			foreach $value (@$item) {
				push(@res,&convert_perlish($value));	
				}
			return "[".join(",",@res)."]";
			}
		if ($type eq "SCALAR") {
			return "\042".&quote_perlish($item)."\042";
			}	
		return "";
		}
	};
	
$encode{'perlish'}=\&convert_perlish;

$decode{'perlish'}=sub {
	my($str)=shift;
	my($res);
	
	return eval($str);
	};

################################################################
###	quoted-printable

$encode{'quoted-printable'}=sub {
	my($str)=shift;
	my(@lines,$item);
	
	
	$str=~s/=/=3D/g;
	$str=~s/([\x00-\x1F\x80-\xFF])/sprintf("=%02X",ord($1))/eg;
	
	if (length($str)>75) {
		while (length($str)>75) {
			if (substr($str,73,1) eq "=") {
				push(@lines,substr($str,0,73)."=");	### soft break
				$str=substr($str,73);
				}
			elsif (substr($str,74,1) eq "=") {
				push(@lines,substr($str,0,74)."=");	### soft break
				$str=substr($str,74);
				}
			else {
				push(@lines,substr($str,0,75)."=");	### soft break
				$str=substr($str,75);
				}
			}
		push(@lines,$str);	
		$str=join($HTTP::crlf,@lines);
		}
		
	return $str;	
	};

$decode{'quoted-printable'}=sub {
	$_[0]=~s/=([0-9a-fA-F][0-9a-fA-F])/pack("C", hex($1))/eg;
	$_[0]=~s/=$crlf//g;
	return $_[0];
	};
1;
