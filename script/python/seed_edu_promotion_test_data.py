#!/usr/bin/env python3
"""
Seed a coherent set of education promotion test data.

The dataset focuses on three scenarios:
1. Normal same-school promotion
2. Terminal-grade pending-advance
3. Missing target class (skip without auto-create, executable with auto-create)

Environment variables:
  DB_HOST
  DB_PORT
  DB_USER
  DB_PASSWORD
  DB_NAME
"""

from __future__ import annotations

import os
from dataclasses import dataclass

import pymysql


SEED_CODE_PREFIX = "PROMO-SEED-"
PARENT_NICKNAME_PREFIX = "升班测试家长-"
STUDENT_NAME_PREFIX = "升班测试"
PASSWORD_HASH = "$2a$04$.vd8nPeLwxt6hnSzmAoAyul8BOLX7Cib6QhcxRe30rfvrIPQHH1OG"


@dataclass
class SchoolSeed:
    key: str
    name: str
    code: str
    area_id: int
    address: str
    years: tuple[tuple[int, int, str, str], tuple[int, int, str, str]]
    grades: list[str]


def get_conn():
    return pymysql.connect(
        host=os.getenv("DB_HOST", "127.0.0.1"),
        port=int(os.getenv("DB_PORT", "3306")),
        user=os.getenv("DB_USER", "root"),
        password=os.getenv("DB_PASSWORD", ""),
        database=os.getenv("DB_NAME", "ruoyi-vue-pro"),
        charset="utf8mb4",
        autocommit=False,
    )


def execute_in(cursor, sql: str, values: list[int]) -> None:
    if not values:
        return
    placeholders = ",".join(["%s"] * len(values))
    cursor.execute(sql.format(placeholders), values)


def cleanup_old_seed_data(cursor) -> None:
    cursor.execute(
        "SELECT id FROM edu_school WHERE code LIKE %s",
        (f"{SEED_CODE_PREFIX}%",),
    )
    school_ids = [row[0] for row in cursor.fetchall()]

    cursor.execute(
        "SELECT id FROM edu_student WHERE student_name LIKE %s",
        (f"{STUDENT_NAME_PREFIX}%",),
    )
    student_ids = {row[0] for row in cursor.fetchall()}

    if school_ids:
        execute_in(
            cursor,
            "SELECT id FROM edu_student WHERE current_school_id IN ({})",
            school_ids,
        )
        student_ids.update(row[0] for row in cursor.fetchall())

    student_ids = sorted(student_ids)

    execute_in(cursor, "DELETE FROM edu_student_flow WHERE student_id IN ({})", student_ids)
    execute_in(cursor, "DELETE FROM edu_student_class WHERE student_id IN ({})", student_ids)
    execute_in(cursor, "DELETE FROM edu_student WHERE id IN ({})", student_ids)

    if school_ids:
        execute_in(cursor, "DELETE FROM edu_school_class WHERE school_id IN ({})", school_ids)
        execute_in(cursor, "DELETE FROM edu_school_grade WHERE school_id IN ({})", school_ids)
        execute_in(cursor, "DELETE FROM edu_school_year WHERE school_id IN ({})", school_ids)
        execute_in(cursor, "DELETE FROM edu_school WHERE id IN ({})", school_ids)

    cursor.execute(
        "DELETE FROM member_user WHERE nickname LIKE %s",
        (f"{PARENT_NICKNAME_PREFIX}%",),
    )


def insert_parent(cursor, nickname: str, mobile: str) -> int:
    cursor.execute(
        """
        INSERT INTO member_user (
            mobile, password, status, register_ip, register_terminal, login_ip, nickname,
            avatar, name, sex, point, experience, creator, updater, tenant_id
        ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """,
        (
            mobile,
            PASSWORD_HASH,
            0,
            "127.0.0.1",
            1,
            "127.0.0.1",
            nickname,
            "",
            nickname.replace(PARENT_NICKNAME_PREFIX, ""),
            0,
            0,
            0,
            "seed-script",
            "seed-script",
            1,
        ),
    )
    return cursor.lastrowid


def insert_school(cursor, school: SchoolSeed) -> int:
    cursor.execute(
        """
        INSERT INTO edu_school (
            school_name, area_id, school_address, code, creator, updater
        ) VALUES (%s, %s, %s, %s, %s, %s)
        """,
        (school.name, school.area_id, school.address, school.code, "seed-script", "seed-script"),
    )
    return cursor.lastrowid


def insert_school_year(cursor, school_id: int, year_start: int, year_end: int, start_date: str, end_date: str) -> int:
    cursor.execute(
        """
        INSERT INTO edu_school_year (
            school_id, year_start, year_end, start_date, end_date, creator, updater
        ) VALUES (%s, %s, %s, %s, %s, %s, %s)
        """,
        (school_id, year_start, year_end, start_date, end_date, "seed-script", "seed-script"),
    )
    return cursor.lastrowid


def insert_school_grade(cursor, school_id: int, grade_catalog_id: int) -> int:
    cursor.execute(
        """
        INSERT INTO edu_school_grade (
            school_id, grade_catalog_id, creator, updater
        ) VALUES (%s, %s, %s, %s)
        """,
        (school_id, grade_catalog_id, "seed-script", "seed-script"),
    )
    return cursor.lastrowid


def insert_school_class(
    cursor,
    school_id: int,
    entry_year: int,
    school_grade_id: int,
    school_year_id: int,
    class_no: int,
    class_name: str,
) -> int:
    cursor.execute(
        """
        INSERT INTO edu_school_class (
            school_id, entry_year, school_grade_id, school_year_id, class_no, class_name, creator, updater
        ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
        """,
        (school_id, entry_year, school_grade_id, school_year_id, class_no, class_name, "seed-script", "seed-script"),
    )
    return cursor.lastrowid


def insert_student(
    cursor,
    student_name: str,
    belong_to: int,
    current_school_id: int,
    entry_year: int,
    student_code: int,
) -> int:
    cursor.execute(
        """
        INSERT INTO edu_student (
            student_name, belong_to, current_school_id, entry_year, student_code, status, creator, updater
        ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
        """,
        (student_name, belong_to, current_school_id, entry_year, student_code, 1, "seed-script", "seed-script"),
    )
    return cursor.lastrowid


def insert_student_class(cursor, student_id: int, class_id: int, start_date: str) -> int:
    cursor.execute(
        """
        INSERT INTO edu_student_class (
            student_id, class_id, start_date, end_date, creator, updater
        ) VALUES (%s, %s, %s, %s, %s, %s)
        """,
        (student_id, class_id, start_date, None, "seed-script", "seed-script"),
    )
    return cursor.lastrowid


def main():
    schools = [
        SchoolSeed(
            key="ht",
            name="海棠实验小学（升班测试）",
            code="PROMO-SEED-HT",
            area_id=320214,
            address="江苏省无锡市新吴区海棠路88号",
            years=((2025, 2026, "2025-09-01", "2026-08-31"), (2026, 2027, "2026-09-01", "2027-08-31")),
            grades=["P5", "P6"],
        ),
        SchoolSeed(
            key="xh",
            name="星河九年一贯制学校（升班测试）",
            code="PROMO-SEED-XH",
            area_id=320205,
            address="江苏省无锡市锡山区星河路66号",
            years=((2025, 2026, "2025-09-01", "2026-08-31"), (2026, 2027, "2026-09-01", "2027-08-31")),
            grades=["P6", "M1", "M2", "M3"],
        ),
        SchoolSeed(
            key="yf",
            name="云帆实验小学（升班测试）",
            code="PROMO-SEED-YF",
            area_id=320206,
            address="江苏省无锡市惠山区云帆路18号",
            years=((2025, 2026, "2025-09-01", "2026-08-31"), (2026, 2027, "2026-09-01", "2027-08-31")),
            grades=["P5", "P6"],
        ),
    ]

    conn = get_conn()
    try:
        with conn.cursor() as cursor:
            cleanup_old_seed_data(cursor)

            cursor.execute(
                """
                SELECT id, grade_no FROM edu_grade_catalog
                WHERE deleted = b'0' AND status = 0
                """
            )
            grade_catalog_map = {grade_no: grade_id for grade_id, grade_no in cursor.fetchall()}

            parent_ids = {}
            for index, (nickname, mobile) in enumerate(
                [
                    ("升班测试家长-王子轩", "13991000001"),
                    ("升班测试家长-李雨桐", "13991000002"),
                    ("升班测试家长-陈嘉禾", "13991000003"),
                    ("升班测试家长-赵一诺", "13991000004"),
                    ("升班测试家长-林星辰", "13991000005"),
                    ("升班测试家长-周沐阳", "13991000006"),
                ],
                start=1,
            ):
                parent_ids[index] = insert_parent(cursor, nickname, mobile)

            school_ids = {}
            school_year_ids = {}
            school_grade_ids = {}
            class_ids = {}

            for school in schools:
                school_id = insert_school(cursor, school)
                school_ids[school.key] = school_id

                year_ids = []
                for year_seed in school.years:
                    year_ids.append(insert_school_year(cursor, school_id, *year_seed))
                school_year_ids[school.key] = year_ids

                grade_ids = {}
                for grade_no in school.grades:
                    grade_ids[grade_no] = insert_school_grade(cursor, school_id, grade_catalog_map[grade_no])
                school_grade_ids[school.key] = grade_ids

            class_ids["ht_p5_src_1"] = insert_school_class(
                cursor, school_ids["ht"], 2021, school_grade_ids["ht"]["P5"], school_year_ids["ht"][0], 1, "2021级五年级1班"
            )
            class_ids["ht_p6_src_1"] = insert_school_class(
                cursor, school_ids["ht"], 2020, school_grade_ids["ht"]["P6"], school_year_ids["ht"][0], 1, "2020级六年级1班"
            )
            class_ids["ht_p6_tgt_1"] = insert_school_class(
                cursor, school_ids["ht"], 2021, school_grade_ids["ht"]["P6"], school_year_ids["ht"][1], 1, "2021级六年级1班"
            )
            class_ids["ht_p6_tgt_2"] = insert_school_class(
                cursor, school_ids["ht"], 2021, school_grade_ids["ht"]["P6"], school_year_ids["ht"][1], 2, "2021级六年级2班"
            )

            class_ids["xh_p6_src_1"] = insert_school_class(
                cursor, school_ids["xh"], 2020, school_grade_ids["xh"]["P6"], school_year_ids["xh"][0], 1, "2020级六年级1班"
            )
            class_ids["xh_m3_src_1"] = insert_school_class(
                cursor, school_ids["xh"], 2023, school_grade_ids["xh"]["M3"], school_year_ids["xh"][0], 1, "2023级初三1班"
            )
            class_ids["xh_m1_tgt_1"] = insert_school_class(
                cursor, school_ids["xh"], 2020, school_grade_ids["xh"]["M1"], school_year_ids["xh"][1], 1, "2020级初一1班"
            )

            class_ids["yf_p5_src_1"] = insert_school_class(
                cursor, school_ids["yf"], 2021, school_grade_ids["yf"]["P5"], school_year_ids["yf"][0], 1, "2021级五年级1班"
            )

            students = [
                ("升班测试王子轩", parent_ids[1], school_ids["ht"], 2021, 2021001, class_ids["ht_p5_src_1"], "2025-09-01"),
                ("升班测试李雨桐", parent_ids[2], school_ids["ht"], 2021, 2021002, class_ids["ht_p5_src_1"], "2025-09-01"),
                ("升班测试陈嘉禾", parent_ids[3], school_ids["ht"], 2020, 2020001, class_ids["ht_p6_src_1"], "2025-09-01"),
                ("升班测试赵一诺", parent_ids[4], school_ids["xh"], 2020, 3020001, class_ids["xh_p6_src_1"], "2025-09-01"),
                ("升班测试林星辰", parent_ids[5], school_ids["xh"], 2023, 3023001, class_ids["xh_m3_src_1"], "2025-09-01"),
                ("升班测试周沐阳", parent_ids[6], school_ids["yf"], 2021, 4021001, class_ids["yf_p5_src_1"], "2025-09-01"),
            ]

            student_ids = {}
            for student_name, belong_to, school_id, entry_year, student_code, class_id, start_date in students:
                student_id = insert_student(cursor, student_name, belong_to, school_id, entry_year, student_code)
                insert_student_class(cursor, student_id, class_id, start_date)
                student_ids[student_name] = student_id

        conn.commit()
        print("Seed data created successfully.")
        print()
        print("Test schools:")
        print(f"  海棠实验小学（升班测试） schoolId={school_ids['ht']} sourceYear={school_year_ids['ht'][0]} targetYear={school_year_ids['ht'][1]}")
        print(f"    Expected: 2 promoted (王子轩, 李雨桐), 1 pending-advance (陈嘉禾)")
        print(f"  星河九年一贯制学校（升班测试） schoolId={school_ids['xh']} sourceYear={school_year_ids['xh'][0]} targetYear={school_year_ids['xh'][1]}")
        print(f"    Expected: 1 promoted across stage (赵一诺 P6->M1), 1 pending-advance (林星辰 M3)")
        print(f"  云帆实验小学（升班测试） schoolId={school_ids['yf']} sourceYear={school_year_ids['yf'][0]} targetYear={school_year_ids['yf'][1]}")
        print(f"    Expected: 1 skipped without auto-create / 1 promoted with auto-create (周沐阳)")
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


if __name__ == "__main__":
    main()
