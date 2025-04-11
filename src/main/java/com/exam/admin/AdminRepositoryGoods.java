package com.exam.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface AdminRepositoryGoods extends JpaRepository<Goods, Integer> {
      /*
          JpaRepository에서 기본으로 제공하는 CRUD 메서드 사용 가능

         - 전체 엔티티 조회 : findAll() - 리턴타입: List
         - 특정 엔티티 조회 : findById(ID id) - 리턴타입: Optional
         - 엔티티 저장 : save(entity)
         - 전체 엔티티 삭제 : deleteAll()
         - 특정 엔티티 id로 삭제 : deleteById(ID id)
         - 특정 엔티티 엔티티로 삭제 : delete(T entity)
         - 엔티티 수정 : 메서드 지원 없이 더티체킹 이용
         - 엔티티 갯수 : count()
       */

	/*
	   관리자가
	   상품 코드 등록(추가)/수정/삭제, - product 테이블 사용
	   개별 상품 등록(추가)/수정/삭제, - goods 테이블 사용
	   행사     등록(추가)/수정/삭제, - 새로 테이블 생성해야함... 행사 테이블 - event 테이블
	   지점     등록(추가)/수정/삭제 기능 구현해야 함 - branch 테이블 사용
	*/
	Optional<Goods> findByGoodsId(int findByGoodsId);

	void deleteByExpirationDateBefore(LocalDateTime now); // 수정된 부분

	// 유통기한이 주어진 날짜보다 이전인 상품들 찾기
	List<Goods> findByExpirationDateBefore(LocalDateTime now);

	@Transactional
	@Modifying
	@Query(value = """
		DELETE FROM goods
		WHERE goodsId IN (
		    SELECT sub.goodsId FROM (
		        SELECT goodsId
		        FROM goods
		        WHERE productCode = :productCode
		          AND branchName = :branchName
		        ORDER BY expirationDate ASC
		        LIMIT :quantity
		    ) AS sub
		)
		""", nativeQuery = true)
	void deleteOldestGoods(
		@Param("productCode") String productCode,
		@Param("branchName") String branchName,
		@Param("quantity") int quantity
	);

}
