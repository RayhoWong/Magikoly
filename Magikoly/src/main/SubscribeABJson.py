"""

 命令模板：python SubscribeABJson.py -f SubscribeAB.xlsx -t ./assets -w SubscribeAb.json
"""

import json
import os
import sys
from importlib import reload
from optparse import OptionParser

import xlrd
from lxml import etree

target_project_path = None


def add_parser():
    parser = OptionParser()

    parser.add_option("-f", "--file_path",
                      help="original.xls File Path.",
                      metavar="file_path", type=str)

    parser.add_option("-t", "--target_path",
                      help="Target Folder Path.",
                      metavar="target_path", type=str)

    parser.add_option("-w", "--output_path",
                      help="Output Json Name",
                      metavar="output_path", type=str)

    (options, args) = parser.parse_args()
    print("options: %s, args: %s" % (options, args))
    return options


def start_convert(options):
    file_path = options.file_path
    target_path = options.target_path
    output_name = options.output_path

    if file_path is not None:
        if target_path is None:
            print("targetPath is None！use -h for help.")
            return
        if output_name != -1:
            global target_project_path
            target_project_path = target_path
            target_path = os.getcwd()
        print("read xls file from: " + file_path)
        xls_file = XlsUtil(file_path)
        table = xls_file.get_table_by_index(0)
        convert_file(table, target_path, output_name)
        print("Finished, see: " + target_path)

    else:
        print().error("file path is None！use -h for help.")


def convert_file(table, target_path, output_name):
    output_json = {
        "success": True,
        "datas": {
            "infos": {
                "filter_id": 8251,
                "abtest_id": 0,
                "cfgs": []
            },
            "message": "ok",
            "status": 200
        }
    }

    first_row = table.row_values(0)
    keys = table.col_values(0)
    # del keys[0]

    for index in range(len(first_row)):
        if index > 0:
            appear_scene = first_row[index]
            values = table.col_values(index)
            # del values[0]
            appear_scene_json = obtain_scene_json(keys, values, appear_scene)
            output_json["datas"]["infos"]["cfgs"].append(appear_scene_json)

    project_path = target_project_path + "/" + output_name
    dir_path = os.path.split(project_path)[0]
    if not os.path.exists(dir_path):
        os.makedirs(dir_path)

    if not os.path.exists(project_path):
        new_file = True
    else:
        new_file = False
        # json_file = open(project_path, "r", encoding="utf8")
        # json_data = json.load(json_file, strict=False)

    if new_file:
        json_file = open(project_path, "a", encoding="utf-8")
    else:
        json_file = open(project_path, "w", encoding="utf-8")

    json.dump(output_json, json_file, ensure_ascii=False)


def obtain_scene_json(keys, values, appear_scene):
    # print("keys="+str(keys))
    # print("values="+str(values))
    result_json = {
        "appear_scene": "0",
        "cfg_tb_id": 0,
        "cfg_id": 1,
        "subscribe_switch": "1",
        "show_style": "0",
        "custom_style": "1",
        "price1": [],
        "price2": [],
        "price3": [],
        "default_price": "2",
        "default_button_effect": "1",
        "close_button_position": "4",
        "if_hijack_return_key": "0",
        "if_hijack_home_key": "0",
        "hijack_return_key": "0",
        "user_property": "0"
    }
    for index in range(len(keys)):
        value_content = values[index]
        if isinstance(value_content,float):
            value_content = int(value_content)
            print("value content="+str(value_content))

        if "/" in keys[index]:
            price_item = keys[index].split("/")
            price_id = price_item[0]
            price_content = price_item[1]
            print("price_item= " + str(price_item))
            if value_content != "null":
                if result_json[price_id]:
                    result_json[price_id][0][price_content] = str(
                        value_content)
                else:
                    price_obj = {
                        "cfg_tb_id": 1,
                        "cfg_id": 1,
                        "price_name": "",
                        "label": "",
                        "service_type": "",
                        "subscribe_id": "",
                        "price_module": ""
                    }
                    price_obj[price_content] = str(value_content)
                    result_json[price_id].append(price_obj)
        else:
            result_json[keys[index]] = str(value_content)

    return result_json


def replaceXMLStr(str):
    result = str.replace("'", "\\'")
    return result


class XlsUtil:
    def __init__(self, file_path):
        self.filePath = file_path
        reload(sys)
        self.data = xlrd.open_workbook(file_path)

    def get_table_by_index(self, index):
        if 0 <= index < len(self.data.sheets()):
            return self.data.sheets()[index]
        else:
            print("XlsUtil error -- getTable: " + index)


def main():
    options = add_parser()
    start_convert(options)


main()
