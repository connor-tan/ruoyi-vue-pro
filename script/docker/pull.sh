#!/usr/bin/env sh
set -u

# 修改为你的三个 Git 仓库路径
REPOS="
/mnt/data/workspace/king/ruoyi-vue-pro
/mnt/data/workspace/king/yudao-ui-admin-vue3
/mnt/data/workspace/king/yudao-mall-uniapp
"

pull_one() {
  repo=$1

  printf '\n========== %s ==========\n' "$repo"

  if ! git -C "$repo" rev-parse --is-inside-work-tree >/dev/null 2>&1; then
    printf 'ERROR: 不是 Git 工作区: %s\n' "$repo" >&2
    return 1
  fi

  branch=$(git -C "$repo" rev-parse --abbrev-ref HEAD 2>/dev/null || printf 'UNKNOWN')
  printf 'branch: %s\n' "$branch"

  # 安全检查：有未提交改动时不 pull，避免覆盖或冲突
  if ! git -C "$repo" diff --quiet || ! git -C "$repo" diff --cached --quiet; then
    printf 'ERROR: 存在未提交改动，已跳过: %s\n' "$repo" >&2
    return 1
  fi

  git -C "$repo" pull --ff-only
}

pids=""

while IFS= read -r repo; do
  [ -z "$repo" ] && continue

  pull_one "$repo" &
  pids="$pids $!"
done <<EOF
$REPOS
EOF

failed=0

for pid in $pids; do
  if ! wait "$pid"; then
    failed=1
  fi
done

if [ "$failed" -eq 0 ]; then
  printf '\n全部仓库更新完成。\n'
else
  printf '\n有仓库更新失败，请检查上面的 ERROR。\n' >&2
fi

exit "$failed"
