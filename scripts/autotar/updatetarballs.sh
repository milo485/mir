#
# this script makes four tarballs:
# 
# 1. STABLE-pre1_0: with and without libs
# 2. current with and without libs

MIR_TARHOME=/home/rk/autotar/tarballs
MIR_HOME=/home/rk/autotar/mir
MIR_STABLEHOME=/home/rk/autotar/mirstable



echo "[updating cvs stable in $MIR_STABLEHOME]"
cd $MIR_STABLEHOME/mir
cvs -q update -dP
cd ..
echo "[tar stable in $MIR_TARHOME] .."
tar cfz $MIR_TARHOME/mir_stable.tar.gz --exclude "mir/lib" --exclude "CVS" mir
echo "[tar stable+libs in $MIR_TARHOME] .."
tar cfz $MIR_TARHOME/mir_stable+libs.tar.gz --exclude "CVS" mir

echo "[updating cvs current in $MIR_HOME]"
cd $MIR_HOME/mir
cvs -q update -dP
cd ..

echo "[tar current in $MIR_TARHOME] .."
tar cfz $MIR_TARHOME/mir.tar.gz --exclude "mir/lib" --exclude "CVS" mir
echo "[tar current+libs in $MIR_TARHOME] .."
tar cfz $MIR_TARHOME/mir+libs.tar.gz --exclude "CVS" mir

