cmake_minimum_required(VERSION 3.4.1)
# 生成的so动态库最后输出的路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/src/main/clibs/${ANDROID_ABI})
#设置变量SRC_DIR为lamemp3的所在路径
set(SRC_DIR src/main/cpp/lamemp3)
#指定头文件所在，可以多次调用，指定多个路径
include_directories(src/main/cpp/lamemp3)
#设定一个目录
aux_source_directory(src/main/cpp/lamemp3 SRC_LIST)
#将前面目录下所有的文件都添加进去
add_library(lame SHARED src/main/cpp/lame_util.cpp ${SRC_LIST})
find_library(clog log)
target_link_libraries(lame ${clog})