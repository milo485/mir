#
# convert.pl
#

sub mk_workfile {
	my($props)=shift;
	local(*HANLDE);
	my($now,$count,$dir,$prefix);

	$dir=$$props{'convert'}{'work_dir'};
	$prefix=$$props{'convert'}{'work_prefix'};
	$now=time;
	$count=0;
	
	while (-f $dir.$prefix.$now.".".$count) { $count++; }

	## fast touch
	if (sysopen(HANDLE,$dir.$prefix.$now.".".$count,O_CREATE|O_RDWR)) { close(HANDLE); }

	return $dir.$prefix.$now.".".$count;
	}

sub html_to_text {
	my($props,$text)=@_;
	local(*HANDLE);
	my($file,$result);

	$file=&mk_workfile($props);
	if (open(HANDLE,">".$file)) {
		print HANDLE $text;
		close(HANDLE);
		}
	else {
		## error - do not convert
		return $text;
		}
	if (open(HANDLE,$$props{'bin'}{'html_to_text'}.$file."|")) {
		while (<HANDLE>) {
			$result.=$_;	
			}
	 	close(HANDLE);	
		unlink($file);
		return $result;
		}	
	else {
		unlink($file);
		return $text;
		}
	}

1;
