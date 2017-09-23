#!/bin/bash

# just exit on error
set -e

if [ $# -eq 0 ]; then
    echo "Usage $0 pass lapack version i.e. 3.1.0"
    exit 1
fi

install -d 3rdparty
cd 3rdparty
echo `pwd`

LAVER=lapack-$1
echo using $LAVER
if [ ! -f "$LAVER.tgz" ]; then
    echo fetch lapack including blas
    wget http://www.netlib.org/lapack/$LAVER.tgz
fi


if [ ! -f mpack-0.8.0.tar.gz ]; then
    echo fetch mpack
    wget 'http://downloads.sourceforge.net/project/mplapack/mpack/mpack%200.8.0/mpack-0.8.0.tar.gz?r=http%3A%2F%2Fmplapack.sourceforge.net%2F&ts=1465988401&use_mirror=ufpr' \
    -O mpack-0.8.0.tar.gz
fi

tar -xzf $LAVER.tgz
tar -xzf mpack-0.8.0.tar.gz

echo set up / clean up maven project
install -d ../blas/src/main/fortran/ ../blas/src/test/java/
install -d ../lapack/src/main/fortran/ ../lapack/src/test/java/
install -d ../mpack/src/main/fortran/ ../mpack/src/main/c/ ../mpack/src/main/cpp/ ../mpack/src/test/java/

rm -rf ../blas/src/main/fortran/*
rm -rf ../lapack/src/main/fortran/* ../mpack/src/main/c/*
rm -rf ../mpack/src/main/fortran/* ../mpack/src/main/c/* ../mpack/src/main/cpp/*

echo copy sources to maven project
set +e
cp $LAVER/BLAS/SRC/* ../blas/src/main/fortran/
cp $LAVER/SRC/* ../lapack/src/main/fortran/
cp $LAVER/SRC/*.f ../lapack/src/main/fortran/
cp $LAVER/SRC/*.c ../lapack/src/main/c/
cp $LAVER/SRC/*.h ../lapack/src/main/c/
cp ../lapack/cblas_mangling.h ../lapack/src/main/c/

echo "curretnt blas version $1" > ../blas/version.txt
echo "curretnt lapack version $1" > ../lapack/version.txt

find -name *.f -exec cp {} ../mpack/src/main/fortran/ \;
find -name *.c -exec cp {} ../mpack/src/main/c/ \;
find -name *.h -exec cp {} ../mpack/src/main/c/ \;
find -name *.cpp -exec cp {} ../mpack/src/main/cpp/ \;
find -name *.h -exec cp {} ../mpack/src/main/cpp/ \;

echo remove inconvenient complex numbers sources
#rm ../lapack/src/main/c/*_z*
rm ../lapack/src/main/fortran/z*.f
