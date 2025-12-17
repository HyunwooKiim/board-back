#!/bin/bash

# 프로젝트 디렉토리
PROJECT_DIR="/Users/gimhyeon-u/Desktop/khwoo1219/develop/important/school/board"
cd "$PROJECT_DIR" || exit 1

# 시작 날짜 (2025년 12월 1일)
START_DATE="2025-12-01"

# 현재 날짜 가져오기
CURRENT_DATE=$(date +%Y-%m-%d)

# 날짜 차이 계산 (일 단위)
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    START_TIMESTAMP=$(date -j -f "%Y-%m-%d" "$START_DATE" +%s)
    CURRENT_TIMESTAMP=$(date -j -f "%Y-%m-%d" "$CURRENT_DATE" +%s)
else
    # Linux
    START_TIMESTAMP=$(date -d "$START_DATE" +%s)
    CURRENT_TIMESTAMP=$(date -d "$CURRENT_DATE" +%s)
fi

DIFF_DAYS=$(( (CURRENT_TIMESTAMP - START_TIMESTAMP) / 86400 ))
DAY_NUMBER=$(( DIFF_DAYS + 1 ))

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📅 현재 날짜: $CURRENT_DATE"
echo "📊 커밋 일차: ${DAY_NUMBER}일차"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# 일차 범위 확인
if [ "$DAY_NUMBER" -lt 1 ]; then
    echo "❌ 아직 커밋 시작일이 아닙니다."
    exit 1
elif [ "$DAY_NUMBER" -gt 17 ]; then
    echo "✅ 모든 커밋이 완료되었습니다!"
    exit 0
fi

# commit.md 파일에서 해당 일차의 커밋 정보 추출
echo ""
echo "📖 commit.md에서 ${DAY_NUMBER}일차 커밋 정보를 읽는 중..."
echo ""

# 해당 일차의 섹션 추출
SECTION=$(awk "/## ${DAY_NUMBER}일차/,/^---$/" "$PROJECT_DIR/commit.md")

if [ -z "$SECTION" ]; then
    echo "❌ ${DAY_NUMBER}일차 정보를 찾을 수 없습니다."
    exit 1
fi

# 커밋 정보 파싱 및 실행
COMMIT_COUNT=0

# 임시 파일에 섹션 저장
echo "$SECTION" > /tmp/commit_section.txt

# 각 커밋 처리
while IFS= read -r line; do
    # 파일 경로 추출
    if [[ "$line" =~ \*\*파일:\*\*[[:space:]]+\`([^\`]+)\` ]]; then
        FILE_PATH="${BASH_REMATCH[1]}"
        # 다음 줄에서 커밋 메시지 읽기
        read -r next_line
        if [[ "$next_line" =~ \*\*커밋\ 메시지:\*\*[[:space:]]+\`([^\`]+)\` ]]; then
            COMMIT_MSG="${BASH_REMATCH[1]}"

            COMMIT_COUNT=$((COMMIT_COUNT + 1))
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            echo "📝 커밋 #${COMMIT_COUNT}"
            echo "📄 파일: $FILE_PATH"
            echo "💬 메시지: $COMMIT_MSG"
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

            # 파일이 존재하는지 확인
            if [ -e "$PROJECT_DIR/$FILE_PATH" ] || [ -d "$PROJECT_DIR/$FILE_PATH" ]; then
                # Git add
                echo "➕ git add $FILE_PATH"
                git add "$FILE_PATH"

                if [ $? -eq 0 ]; then
                    # Git commit
                    echo "✍️  git commit -m \"$COMMIT_MSG\""
                    git commit -m "$COMMIT_MSG"

                    if [ $? -eq 0 ]; then
                        echo "✅ 커밋 성공!"
                    else
                        echo "❌ 커밋 실패!"
                        exit 1
                    fi
                else
                    echo "❌ git add 실패!"
                    exit 1
                fi
            else
                echo "⚠️  경고: 파일/디렉토리를 찾을 수 없습니다: $FILE_PATH"
                echo "   (스킵합니다)"
            fi
            echo ""
        fi
    fi
done < /tmp/commit_section.txt

# 정리
rm -f /tmp/commit_section.txt

if [ "$COMMIT_COUNT" -eq 0 ]; then
    echo "❌ 처리할 커밋이 없습니다."
    exit 1
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ ${DAY_NUMBER}일차 총 ${COMMIT_COUNT}개 커밋 완료!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Git push
read -p "🚀 원격 저장소에 푸시하시겠습니까? (y/n): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "📤 git push origin main"
    git push origin main

    if [ $? -eq 0 ]; then
        echo "✅ 푸시 성공!"
    else
        echo "❌ 푸시 실패!"
        exit 1
    fi
else
    echo "⏭️  푸시를 건너뜁니다."
fi

echo ""
echo "🎉 모든 작업이 완료되었습니다!"
