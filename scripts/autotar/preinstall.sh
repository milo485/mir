mkdir mir
mkdir mirstable
mkdir tarballs

#
# stable part

cd mirstable
cvs co -r STABLE-pre1_0 co
cd ..

#
# unstable part

cd mir
cvs co mir
cd ..

