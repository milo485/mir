#!/usr/bin/perl

################################################################
###	Preprocessor
###
### m
###
### $VER: 3.1.1 (% notation)$

require "codec.pl";

###########################################################	
### token stuff

package TokenStream;

sub new {
	local(*IN,$intro,$outro)=@_;
	
	$$stream{'handle'}=*IN;
	$$stream{'buffer'}=[];
	$$stream{'intro'}=$intro;
	$$stream{'outro'}=$outro;
	
	bless $stream, "TokenStream";
	
	return $stream;
	}
	
sub fetch {
	my($stream)=shift;
	my($token,$old);
	local(*IN);
	
	if (defined ($token=shift(@{$$stream{'buffer'}}))) {
		return $token;
		}
	else {
		$old=$/;
		$/=$$stream{'outro'};
		*IN=$$stream{'handle'};
		
		$token=<IN>;
		
		if (($index=index($token,$$stream{'intro'}))>0) {
			push(@{$$stream{'buffer'}},substr($token,$index));
			$token=substr($token,0,$index);
			}
		
		$/=$old;
		
		return $token;
		}	
	}	

sub feed {
	my($stream,$value)=@_;
	
	if (ref $value) {
		unshift(@{$$stream{'buffer'}},@$value);
		}
	else {
		unshift(@{$$stream{'buffer'}},$value);	
		}
	}
	
###########################################################	
### prepro stuff

package Subst;
	
###########################################################	
### local fun :-)
	
$localfun{'store'}=sub {
	my($args,$env,$stream)=@_;
	my($token,$field,$count);
	
	# print  "STORE $$args[1]\n";
	
	$field=$$args[1];
	$count=0;
	
	$$env{$field}{'_data'}=[];
	while (($token=$stream->fetch) && ($token ne "<%/store%>")) {
		push(@{$$env{$field}{'_data'}},$token);
		$count++;
		}
	$$env{$field}{'_pos'}=0;
	$$env{$field}{'_size'}=$count;	
	}; 
	
$localfun{'reset'}=sub {
	my($args)=shift;
	
	$$env{$$args[1]}{'_pos'}=0;
	};	
	
$localfun{'insert'}=sub {
	my($args,$env,$stream)=@_;
	
	#print main::DEBUG "INSERT $$args[1] size=",scalar(@{$$env{$$args[1]}{'_data'}})," time=$$env{$$args[1]}{'_pos'}\n";
	
	$stream->feed($$env{$$args[1]}{'_data'});
	$$env{$$args[1]}{'_pos'}++;
	};	

$localfun{'loop'}=sub {
	my($args,$env,$stream)=@_;
	my($token,@loop,$count);
	
	
	$count=$$args[1];
	#print "LOOP $count\n";
	while (($token=$stream->fetch) && ($token ne "<%/loop%>")) {
		push(@loop,$token);
		}
	while ($count--) {
		$stream->feed(\@loop);
		}	
	};

sub clone {
	my(@data)=@_;
	return \@data;
	}
	
$localfun{'load'}=sub {
	my($args,$env,$stream)=@_;
	my(@temp,$count);
	
	# print "<!-- LOAD: $$args[2] into $$args[1] -->";
	
	$$env{$$args[1]}{'_data'}=[];
	$count=0;
	if (open(LOAD,$$args[2])) {
		while (<LOAD>) {
			chomp;
			#print "ROW: $_\n";
			@temp=split(/\t/);
			push(@{$$env{$$args[1]}{'_data'}},&clone(@temp));
			undef @temp;
			$count++;
			}
		close(LOAD);
		}
	$$env{$$args[1]}{'_size'}=$count;
	$$env{$$args[1]}{'_pos'}=0;
	
	# print "LOAD $$args[2] into $$args[1] ($count)\n";
	};

$localfun{'loadvalues'}=sub {
	my($args,$env,$stream)=@_;
	local(*LOAD);
	my($cat)="public";
	my($slot,$key,$value,@collect);
	
	if (open(LOAD,$$args[1])) {
		while (<LOAD>) {
			s/[\r\n]+//g;
			next if /^\s*$/;
			next if /^#/;
			
			if (/^\[(\w+)\]/) {
				$cat=$1;
				next;
				}
			if (/^\[(\w+)\]\s*=\>\s*\[(\w+)\]$/) {
				$$env{$2}=$$env{$1};
				next;
				}
			if (/([^=\s]+)\s*=\s*(.*?)\s*$/) {
				$$env{$cat}{$1}=$2;	
				next;
				}
			if (/([^=\s]+)\s*:\s*(.*)/) {
				$key=$1; $value=$2;
				$value=~s/\$\((\w+)\)/$ENV{$value}/e;
				$$env{$cat}{$key}=$value;	
				next;
				}
			if (/([^\<\s]+)\s*\<\<\s*(.*)/) {
				$slot=$1;
				undef @collect;
				while (<LOAD>) {
					last if substr($_,0,2) eq "<<";
					push(@collect,$_);
					}
				$$env{$cat}{$slot}=join("\n",@collect);	
				}
			}
		close(LOAD);	
		}
	else {
		print "Cannot loadvalues ".$$args[1]."\n";
		}
	};
	
$localfun{'bind'}=sub {
	my($args,$env,$stream)=@_;
	my($temp,$name,$var,$t,$item);

	shift(@$args);	# function name
	$name=shift(@$args);
	$var=shift(@$args);
	
	#print "BIND from $name [$$env{$name}{'_pos'}] to $var : ";
	
	if ($$env{$name}{'_pos'}<$$env{$name}{'_size'}) {
		$t=0;
		$temp=$$env{$name}{'_data'}[$$env{$name}{'_pos'}];
		foreach $item (@$args) {
			#print "\t$item=$$temp[$t]\n";
			$$env{$var}{$item}=$$temp[$t++];
			}
		$$env{$name}{'_pos'}++;
		}
	else {
		undef $$env{$var};
		}	
	};	

### version 3.1 functions

$localfun{'incr'}=sub {
	my($args)=shift;
	
	#print main::DEBUG "INCR $$args[1]\n";
	
	$$env{$$args[1]}{'_pos'}++;
	};	

$localfun{'alias'}=sub {
	my($args,$env,$stream)=@_;
	my($temp,$name,$var,$t,$item);

	shift(@$args);	# function name
	$name=shift(@$args);
	$var=shift(@$args);
	
	#print main::DEBUG "ALIAS from $name [$$env{$name}{'_pos'}] to $var\n";
	
	if ($$env{$name}{'_pos'}<$$env{$name}{'_size'}) {
		$$env{$var}=$$env{$name}{'_data'}[$$env{$name}{'_pos'}];
		}
	else {
		undef $$env{$var};
		}	
	};	

$localfun{'keyvalue'}=sub {
	my($args,$env,$stream)=@_;
	my($from,$to,@buffer,$key,$value);

	shift(@$args);	# function name
	$from=shift(@$args);
	$to=shift(@$args);
	
	
	while (($key,$value)=each %{$$env{$from}}) {
		push(@buffer,{'key' => $key, 'value' => $value});
		}
	$$env{$to}{'_data'}=\@buffer;
	$$env{$to}{'_pos'}=0;
	$$env{$to}{'_size'}=scalar(@buffer);	
	
	#print main::DEBUG "keyvalue $from into $to ",scalar(@buffer),"\n";
	};
	
$localfun{'set'}=sub {
	my($args,$env,$stream)=@_;
	my($name,$value);

	shift(@$args);	# function name
	$name=shift(@$args);
	$value=shift(@$args);
	
	$$env{'_flags'}{$name}=$value;
	};	
	
###########################################################	
### if fun ??

$iffun{'ifdef'}=sub {
	my($args,$env)=@_;
	return ((defined (&getvalue($env,$$args[1]))) ? 0 : 1);
	};

$iffun{'ifnz'}=sub {
	my($args,$env)=@_;
	return (&getvalue($env,$$args[1]) ? 0 : 1);
	};
	
$iffun{'ifequal'}=sub {
	my($args,$env)=@_;
	return ((&getvalue($env,$$args[1]) eq $$args[2]) ? 0 : 1);
	};	
		
$iffun{'ifmember'}=sub {
	my($args,$env)=@_;
	return ((index($$args[1],$$args[2])<0) ? 1 : 0);
	};	
		
$iffun{'ifdecr'}=sub {
	my($args,$env)=@_;
	
	if ($$args[1]=~/(\S+)\.(\S+)/) {
		return (($$env{$1}{$2}-- > 0) ? 0 : 1);
		}		
	else { 
		return (($$env{$$args[1]}-- > 0) ? 0 : 1);
		}
	};
	
$iffun{'ifiter'}=sub {
	my($args,$env)=@_;
	
	if ($$args[2]=~/(\d+)\.(\d+)/) {
		return (($$env{$$args[1]}{'_pos'} % $1) != $2);
		}		
	else { 
		return (($$env{$$args[1]}{'_pos'} % $$argv[2]) != 0);
		}
	};


### version 3.1 functions

$iffun{'ifplus'}=sub {
	my($args,$env)=@_;
	return ((&getvalue($env,$$args[1])>0) ? 0 : 1);
	};

$iffun{'ifminus'}=sub {
	my($args,$env)=@_;
	return ((&getvalue($env,$$args[1])<0) ? 0 : 1);
	};

$iffun{'ifpluszero'}=sub {
	my($args,$env)=@_;
	return ((&getvalue($env,$$args[1])>=0) ? 0 : 1);
	};
	
$iffun{'ifminuszero'}=sub {
	my($args,$env)=@_;
	return ((&getvalue($env,$$args[1])<=0) ? 0 : 1);
	};

	
###########################################################	
### process

sub process {
	local(*IN,*OUT,$env,$post)=@_;
	my($token,$ignore,@temp,$templine,$stream);
	
	$logfile=$$env{'logfile'};
	
	$stream=&TokenStream::new(*IN,"<%","%>");
	
	return undef unless defined $stream;
	
	#if (!defined $post) { $post=sub { print OUT $_[0] unless $_[0]=~/^\s*$/; }; }
	if (!defined $post) { $post=sub { print OUT $_[0]; }; }
	
	$ignore=0;
	
	while ($token=$stream->fetch) {
		# if-endif
		if	($token eq "<%endif%>") {
			$ignore-- if $ignore>0; 
			next; 
			}

		if (($ignore>0) && (substr($token,0,4) eq "<%if")) {
			$ignore++;
			next;
			}
			
		if ($token eq "<%else%>") {
			($ignore == 0) && ($ignore=1, next);
			($ignore == 1) && ($ignore=0, next);
			}
				
		next if $ignore;
		
		#$token=~s/\^(\'?[\w.]+)(\\)?/&getvalue($env,$1)/eg;
		#$token=~s/\^(\[\w+\])?([\w.]+)(\\)?/&getvalue($env,$2,$1)/eg;
		$token=~s/\^(\[\w+\])?([\w\x5b\x5d.]+)(\\)?/&getvalue($env,$2,$1)/eg;
		
		# handle none-special case
		if (substr($token,0,2) ne "<%") {
			&$post($token);
			next;
			}
		
		@temp=split(/\s+/,substr($token,2,-2));
		
		if (defined $iffun{$temp[0]}) {
			$ignore=&{$iffun{$temp[0]}}(\@temp,$env);
			next;
			}
			
		if (defined $localfun{$temp[0]}) {
			&{$localfun{$temp[0]}}(\@temp,$env,$stream);
			next;
			}
		
		&$post($token);
		}	
	}

###########################################################
### binding

$perlish=&Codec::new('perlish');

$convert{'[perl]'}=sub { return $perlish->encode(@_); };

$convert{'[scalar]'}=sub { return scalar($_[0]); };

$convert{'[javastyle]'}=sub {
	my($value)=shift;
	$value=lc($value);
	substr($value,0,1)=uc(substr($value,0,1));
	return $value;
	};
	
sub getvalue {
	my($env,$key,$fun)=@_;
	my($convert,$value);
	
#	$key=~s/\./\'\}\{\'/g;
#	$value=eval "\$\$env{'$key'}";
	
	$key=~s/\./\'\}\{\'/g;
	$key="{'".$key."'}";
	$key=~s/(\[\d+\])\'\}/\'\}$1/g;
	$value=eval "\$\$env$key";

	if ((defined $fun) && (defined $convert{$fun})) { return &{$convert{$fun}}($value); }
	else		  { return $value; }
	}

1;
