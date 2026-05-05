"""
Generate SHIFT + SHIFT_SERVICE_INFO inserts for postgres_test (May 2026).

Mirrors DepartmentConverter.departmentFromDate() with anchor 2026-02-22.
Uses only SERVICE_INFO belonging to dept 1/2 users (excluding NULL department users).

Emit SQL to stdout. Example:
  python core/scripts/generate_may2026_shift_injection.py > shifts_may2026.sql
  docker exec -i shtopor-postgres_test-1 psql -U test_user -d vzvod_db_test -f shifts_may2026.sql
"""

from __future__ import annotations

import uuid
from datetime import date, timedelta


MONTH_START = date(2026, 5, 1)
MONTH_END = date(2026, 5, 31)
CYCLE_ANCHOR = date(2026, 2, 22)

SERVICE_INFO_IDS: dict[int, dict[str, str]] = {
    1: {
        "gres": "019dda13-01e0-7195-b452-0433ab276801",
        "kuhor": "019dda1c-b554-766a-9add-c847afda1b1c",
        "sadov": "019dda11-8011-786f-8447-af031eca8aa2",
        "tihon": "019dda33-8714-7da5-8955-8ee7aaed1ad4",
    },
    2: {
        "kovalchuk": "019dd817-7f4c-70a8-ad9b-f05447ede98e",
        "pavlov": "019dd815-92cf-7fb9-9f51-f3f7ca2a0282",
        "tarasov": "019dd5bb-bc7c-7483-b868-7406ae38cc7f",
    },
}

PLATOON_ROUTES = [("МП 28", "VZVOD_ROUTE"), ("МП 30", "VZVOD_ROUTE"), ("МП 31", "VZVOD_ROUTE")]
BAT_ROUTES = [("МП 32", "BAT_POST"), ("МП 5", "BAT_POST"), ("МП 6", "BAT_POST"), ("СП 3", "BAT_POST"), ("СП 18", "BAT_POST")]


def dept_on_day(d: date) -> int:
    diff = (d - CYCLE_ANCHOR).days
    normalized = ((diff % 4) + 4) % 4
    return 1 if normalized in (0, 1) else 2


def dept1_partitions(seq: int) -> tuple[tuple[str, str], tuple[str, str]]:
    ids = ["gres", "kuhor", "sadov", "tihon"]
    patterns = [(0, 1, 2, 3), (0, 2, 1, 3), (0, 3, 1, 2)]
    a, b, c, e = patterns[seq % len(patterns)]
    return (ids[a], ids[b]), (ids[c], ids[e])


def lcg(seed: int) -> int:
    """Deterministic pseudo-random int generator step."""
    return (1103515245 * seed + 12345) & 0x7FFFFFFF


def rand_int(lo: int, hi: int, seed: int) -> tuple[int, int]:
    if lo > hi:
        lo, hi = hi, lo
    s = lcg(seed)
    span = hi - lo + 1
    return lo + (s % span), s


def split_nonnegative(sum_total: int, parts: int, seed: int) -> list[int]:
    if parts <= 0:
        return []
    if parts == 1:
        return [sum_total]

    leftover = sum_total
    out: list[int] = []
    s = seed
    for _ in range(parts - 1):
        s = lcg(s)
        # choose cut in [0, leftover]; bias toward nonzero when leftover large
        if leftover == 0:
            cut = 0
        else:
            cap = leftover + 1
            cut = s % cap
        out.append(cut)
        leftover -= cut
    out.append(leftover)
    return out


def pick_route(day: date, dept: int, patrol_idx_global: int) -> tuple[str, str]:
    k = day.toordinal() + dept * 109 + patrol_idx_global * 37 + 919
    platoon_pick = (((k >> 3) ^ (k >> 11)) % 11) >= 5
    pool = PLATOON_ROUTES if platoon_pick else BAT_ROUTES
    return pool[(k >> 5) % len(pool)]


def dept2_two_patrols(seq: int) -> tuple[tuple[str, str], tuple[str, str]]:
    """Two overlaps across 3 people; rotate which pair overlaps 'kovalchuk'."""
    patterns = (
        (("kovalchuk", "pavlov"), ("kovalchuk", "tarasov")),
        (("kovalchuk", "tarasov"), ("pavlov", "tarasov")),
        (("kovalchuk", "pavlov"), ("pavlov", "tarasov")),
        (("kovalchuk", "tarasov"), ("kovalchuk", "pavlov")),
        (("pavlov", "tarasov"), ("kovalchuk", "pavlov")),
    )
    return patterns[seq % len(patterns)]


def esc(sql: str) -> str:
    return sql.replace("'", "''")


def main() -> None:
    patrol_idx_global = 0
    d1_seq = 0
    d2_seq = 0

    print("-- May 2026 shift simulation (anchors to DepartmentConverter 2026-02-22)")
    print("-- Clears shifts in interval [2026-05-01, 2026-06-01)")
    print("BEGIN;\n")

    print(
        """
-- Отвязать записи практики от удаляемых смен (shift_id допускает NULL), не удалять сами нарушения.
UPDATE administrative_violation SET shift_id = NULL
WHERE shift_id IN (
  SELECT id FROM shift WHERE date >= DATE '2026-05-01' AND date < DATE '2026-06-01'
);

UPDATE criminal_violation SET shift_id = NULL
WHERE shift_id IN (
  SELECT id FROM shift WHERE date >= DATE '2026-05-01' AND date < DATE '2026-06-01'
);

DELETE FROM shift_service_info
WHERE shift_id IN (
  SELECT id FROM shift WHERE date >= DATE '2026-05-01' AND date < DATE '2026-06-01'
);

DELETE FROM shift WHERE date >= DATE '2026-05-01' AND date < DATE '2026-06-01';
"""
    )

    seed_base = MONTH_START.toordinal()

    cur = MONTH_START
    while cur <= MONTH_END:
        dept = dept_on_day(cur)

        seed = seed_base ^ (cur.day * 17) ^ (cur.month << 11) ^ (dept << 21)

        if dept == 1:
            pair_a, pair_b = dept1_partitions(d1_seq)
            patrol_specs = [pair_a, pair_b]
            d1_seq += 1
            npatrols = len(patrol_specs)
        else:
            draw, _ = rand_int(0, 99, seed ^ 777)
            use_dual = draw < 38  # ~38% дней — два наряда; остальное — один патруль из пары
            if use_dual:
                pa, pb = dept2_two_patrols(d2_seq)
                patrol_specs = [pa, pb]
            else:
                duo = (
                    ("kovalchuk", "pavlov"),
                    ("kovalchuk", "tarasov"),
                    ("pavlov", "tarasov"),
                )[d2_seq % 3]
                patrol_specs = [duo]
            d2_seq += 1
            npatrols = len(patrol_specs)

        # AP per patrol 0..4
        ap_each: list[int] = []
        s = seed ^ 913
        for _ in range(npatrols):
            v, s = rand_int(0, 4, s ^ 0xABCDEF)
            ap_each.append(v)

        # Claims (УП): 0-2/day for dept, sparse
        cseed = seed ^ 444

        wt0, cseed = rand_int(0, 100, cseed)
        if wt0 < 54:
            ctot = 0
        elif wt0 < 88:
            ctot = 1
        else:
            ctot = 2

        claim_each = [0] * npatrols
        if ctot == 0:
            claim_each = [0] * npatrols
        elif ctot == 1:
            sel, _ = rand_int(0, npatrols - 1, cseed ^ 101)
            claim_each[sel] = 1
        else:
            if npatrols == 1:
                claim_each[0] = 2
            else:
                claim_each = [1, 1]

        ibdr_day, ibseed = rand_int(20, 40, seed ^ 2026)
        # split migrant/with vs without totals
        mshare, ibseed = rand_int(35, 65, ibseed ^ 111)
        with_total = round(ibdr_day * (mshare / 100))
        if with_total > ibdr_day:
            with_total = ibdr_day
        if with_total < 0:
            with_total = 0
        wo_total = ibdr_day - with_total

        with_each = split_nonnegative(with_total, npatrols, ibseed ^ 333)
        wo_each = split_nonnegative(wo_total, npatrols, ibseed ^ 999)

        for i in range(npatrols):
            user_a, user_b = patrol_specs[i]
            si_a = SERVICE_INFO_IDS[dept][user_a]
            si_b = SERVICE_INFO_IDS[dept][user_b]

            route_no, route_type = pick_route(cur, dept, patrol_idx_global)
            shift_id = str(uuid.uuid4())

            stmt = esc(str(ap_each[i]))
            clm = str(claim_each[i])
            iw = str(with_each[i])
            inw = str(wo_each[i])

            nm = esc(route_no)
            tot = esc(route_type)

            print(
                "INSERT INTO shift ("
                "id,type_of_shift,date,start_time,end_time,"
                "count_of_statements,count_of_claims,ibd_with_migrant,ibd_without_migrant,"
                "department,number"
                ") VALUES ("
                f"'{shift_id}',"
                f"'{tot}',"
                f"'{cur.isoformat()}'::date,"
                f"'09:00:00'::time,"
                f"'21:00:00'::time,"
                f"{stmt},"
                f"{clm},"
                f"{iw},"
                f"{inw},"
                f"{dept},"
                f"'{nm}'"
                ");"
            )

            print(
                "INSERT INTO shift_service_info (shift_id,service_info_id) VALUES "
                f"('{shift_id}','{si_a}'),('{shift_id}','{si_b}');"
            )

            patrol_idx_global += 1

        cur += timedelta(days=1)

    print("\nCOMMIT;")


if __name__ == "__main__":
    main()
