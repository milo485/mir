#!/usr/bin/perl

use CGI qw(:standard);
use LWP::UserAgent;
use URI::Escape;

#this program takes, as CGI params, a query string "words"
#a syntax string "bool" (and|or), values of Y or N for the 
#variables of the form hasVideo, hasAudio, hasImage, etc.
#and a variable topic corresponding to the desired topic title
#it then munges these variables into an appropriate request to htdig,
# does this request and forwards the results to the user 

$searchhost='http://prod.indymedia.nl/cgi-bin/htsearch';

$bool=param('bool');
$hasVideo=param('hasVideo');
$hasAudio=param('hasAudio');
$hasImages=param('hasImages');
$topic=param('topic');
@topic_words=split(/\s+/,$topic);
@words=();

$querywords='';
@words=split (/\s+/,param('words')) if param('words');
push @words,@topic_words;
if (@words){

if ($bool eq "and"){
$querywords=join " AND ", @words;  
}
else{
$querywords=join " OR ", @words;  
}

$querywords =~ s/\(\)/ /g;
$querywords="($querywords)";
}

if ($hasVideo eq "Y" || $hasAudio eq "Y" || $hasImages eq "Y"){
    $first=1;
    if (@words){
    $querywords.=" AND ";
}
    $querywords .= "(";

if ($hasVideo eq "Y"){
    if (!$first){
	$querywords.=" OR";
    }
    $querywords.=" RealVideo OR Video"; 
    $first=0;
} 

if ($hasAudio eq "Y"){
    if (!$first){
	$querywords.=" OR";
    }    
    $querywords.=" RealAudio OR Audio"; 
    $first=0;
} 

if ($hasImages eq "Y"){
    if (!$first){
	$querywords.=" OR";
    }
    $querywords.=" ImagesGif OR ImagesJpeg";
    $first=0;
}
	$querywords.=" )";
}

$querywords=uri_escape($querywords,"^A-Za-z0-9");

$query="words=$querywords&format=builtin-long&sort=score&method=boolean";

# propagate the config parameter if it is set - rob
$config=param('config');
$query.="&config=$config" if $config;



$ua = new LWP::UserAgent;
$ua->agent("AgentName/0.1 " . $ua->agent);

# Create a request
my $req = new HTTP::Request POST => $searchhost;
$req->content_type('application/x-www-form-urlencoded');
$req->content($query);

# Pass request to the user agent and get a response back
my $res = $ua->request($req);

# Check the outcome of the response
if ($res->is_success) {
    print header;
    print $res->content;
} else {
    print header;
    print "Search engine temporarily unavailable\n";
}
