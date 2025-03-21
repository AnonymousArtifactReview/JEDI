#!/bin/bash

thisdir=$(readlink -f "${BASH_SOURCE[0]%/*}")
source $thisdir/_common.sh


wget_or_exit() {
    local url="$1"
    wget "$url" || { echo "Error: Failed to download $url"; exit 1; }
}


extract_and_move() {
    local archive="$1"
    local destination="$2"
    if [ -d $destination ]; then
      rm -rf $destination
    fi
    tar xf "$archive" || { echo "
    }Error extracting $archive"; exit 1; }
    local folder_name=$(tar tf "$archive" | head -1 | cut -d/ -f1)
    mv $folder_name $destination
}


arch=$(dpkg --print-architecture)
if [ "$arch" = "amd64" ]; then
  jdk21file="jdk-21_linux-x64_bin.tar.gz"
  jdk23file="jdk-23_linux-x64_bin.tar.gz"
  graalvmfile="graalvm-jdk-23_linux-x64_bin.tar.gz"
elif [ "$arch" = "arm64" ]; then
  jdk21file="jdk-21_linux-aarch64_bin.tar.gz"
  jdk23file="jdk-23_linux-aarch64_bin.tar.gz"
  graalvmfile="graalvm-jdk-23_linux-aarch64_bin.tar.gz"
else
  echo "Unsupported architecture: $arch"
fi

mkdir -p "$JVM_DIR"

rm *.gz

wget_or_exit "https://download.oracle.com/java/21/latest/$jdk21file"
wget_or_exit "https://download.oracle.com/java/23/latest/$jdk23file"
wget_or_exit "https://download.oracle.com/graalvm/23/latest/$graalvmfile"

extract_and_move $jdk21file $JDK21_DIR
extract_and_move $jdk23file $JDK23_DIR
extract_and_move $graalvmfile $GRAALVM_DIR

