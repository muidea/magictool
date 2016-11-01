package main

import (
	"encoding/json"
	"html/template"
	"io"
	"log"
	"martini"
	"net/http"
	"os"
	"path"
	"path/filepath"
	"strings"
)

type Result struct {
	ErrCode int
	Reason  string
}

type FileInfo struct {
	FileName   string
	UploadTime string
}

type FileInfoList struct {
	FileList []FileInfo
}

// MultipartFormFile 接受文件参数
// 上传的文件名
// 上传文件扩展名
// 错误
func MultipartFormFile(r *http.Request, field, dstPath string) (string, string, error) {
	dstFile := ""
	fileType := ""
	var err error

	for true {
		src, head, err := r.FormFile(field)
		if err != nil {
			break
		}
		defer src.Close()

		_, err = os.Stat(dstPath)
		if err != nil {
			err = os.MkdirAll(dstPath, os.ModeDir)
		}
		if err != nil {
			break
		}
		dstFile = path.Join(dstPath, head.Filename)
		dst, err := os.Create(dstFile)
		if err != nil {
			break
		}

		defer dst.Close()
		_, err = io.Copy(dst, src)

		fileInfo, err := os.Stat(dstFile)
		if err == nil {
			items := strings.Split(fileInfo.Name(), ".")
			cnt := len(items)
			if cnt >= 2 {
				fileType = items[cnt-1]
			} else {
				fileType = "unknown"
			}
		}
		break
	}

	return dstFile, fileType, err
}

func walkPath(filePath string) ([]string, error) {
	fileList := []string{}
	err := filepath.Walk(filePath, func(path string, f os.FileInfo, err error) error {
		if f == nil {
			return err
		}

		if f.IsDir() {
			return nil
		}

		fileList = append(fileList, path)
		return nil
	})

	return fileList, err
}

func mainView(res http.ResponseWriter, req *http.Request) {
	log.Printf("indexHandler")

	res.Header().Set("content-type", "text/html")
	res.Header().Set("charset", "utf-8")

	t, err := template.ParseFiles("public/html/index.html")
	if err != nil {
		panic("ParseFiles failed, err:" + err.Error())
	}

	t.Execute(res, nil)
}

func listFile(res http.ResponseWriter, req *http.Request) {

	result := FileInfoList{}

	staticPath := "./public"
	uploadPath := "upload"
	filePath := path.Join(staticPath, uploadPath)

	fileList, err := walkPath(filePath)
	if err == nil {
		for _, file := range fileList {
			info, err := os.Stat(file)
			if err == nil {
				fileInfo := FileInfo{}
				fileInfo.FileName = info.Name()
				fileInfo.UploadTime = info.ModTime().Format("2006-01-02 15:04:05")
				result.FileList = append(result.FileList, fileInfo)
			}
		}
	}

	b, err := json.Marshal(result)
	if err != nil {
		panic("json.Marshal, failed, err:" + err.Error())
	}

	res.Write(b)
}

func uploadFile(res http.ResponseWriter, req *http.Request) {

	result := Result{}

	for true {
		err := req.ParseMultipartForm(0)
		if err != nil {
			result.ErrCode = 1
			result.Reason = "无效请求数据123"
			break
		}

		staticPath := "./public"
		tempPath := "tmp"
		uploadPath := "upload"
		//filePath := path.Join(staticPath, tempPath, time.Now().Format("20060102150405"))
		tempPath = path.Join(staticPath, tempPath)
		uploadfile, _, err := MultipartFormFile(req, "userfile", tempPath)
		if err != nil {
			result.ErrCode = 1
			result.Reason = "无效请求数据"
			break
		}

		fileInfo, err := os.Stat(uploadfile)
		if err != nil {
			result.ErrCode = 1
			result.Reason = "处理错误"
			break
		}

		itemArray := strings.Split(fileInfo.Name(), ".")
		itemArray = strings.Split(itemArray[0], "_")

		filePath := path.Join(staticPath, uploadPath, itemArray[0], itemArray[2])
		_, err = os.Stat(filePath)
		if err != nil {
			os.MkdirAll(filePath, os.ModeDir)
		}
		dstFile := path.Join(filePath, fileInfo.Name())
		err = os.Rename(uploadfile, dstFile)
		if err != nil {
			result.ErrCode = 1
			result.Reason = "处理错误"
			break
		}

		result.ErrCode = 0
		result.Reason = "保存成功"
		break
	}

	b, err := json.Marshal(result)
	if err != nil {
		panic("json.Marshal, failed, err:" + err.Error())
	}

	res.Write(b)
}

func main() {
	os.Stdout.WriteString("=================================\n")
	os.Stdout.WriteString("Magic Tool Web Server V1.0\n")
	os.Stdout.WriteString("Author:rangh\n")
	os.Stdout.WriteString("EMail:rangh@foxmail.com\n")
	os.Stdout.WriteString("=================================\n")
	os.Setenv("PORT", "8000")

	m := martini.Classic()

	m.Get("/", mainView)

	m.Get("/list/", listFile)

	m.Post("/upload/", uploadFile)

	m.Run()
}
