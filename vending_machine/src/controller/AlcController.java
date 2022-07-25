package controller;

import java.util.ArrayList;

import model.AlcDAO;
import model.AlcVO;
import model.CategoryVO;
import view.AlcView;

public class AlcController {

	AlcDAO model = new AlcDAO();
	AlcView view = new AlcView();

	public void startApp() {
		while (true) {

			if (view.userAge()) { // 시작시 나이 인증
				while (true) {
					view.user_startView(); // 자판기 실행메뉴 선택

					if (view.action == 1) { // 메뉴확인
						AlcVO vo = new AlcVO();
						view.menu(model.selectAll(vo)); // 전체메뉴 출력

					} else if (view.action == 2) { // 구매하기
						AlcVO vo = new AlcVO();
						CategoryVO cate = new CategoryVO();
						ArrayList<AlcVO> datas = model.selectAll(vo);
						ArrayList<CategoryVO> catedatas = model.categoryAll(cate);

						int choose = view.categoryMenu(catedatas); // 대분류 카테고리 출력 & 원하는 카테고리 선택

						cate.setName(catedatas.get(choose - 1).getName());

						if (model.categoryOne(cate) == null) {
							view.fail();
							continue;
						}

						view.sool(datas, cate); // 전체 배열에서 이름이 일치하는 소분류 카테고리 출력

						// 구매할 술 이름 입력받기
						while (true) {
							view.buyName(); // 소분류 카테고리에서 술 이름 입력 받기
							vo.setName(view.inputString()); // 구매할 술이름 입력
							AlcVO vo2 =model.selectOne(vo);
							if (vo2 == null) { // 이름이 없을 때
								view.buyNameFail();
								continue;
							} 
							if (vo2.getCnt() == 0) { // 재고가 없을 때
								view.cntF();
								continue;
							} 
							break;

						}

						// 재고 확인하기
						while (true) {
							view.buyCnt(); // 이름이 있다면 재고 입력 받기
							vo.setCnt(view.inputInt()); // 구매할 수량 입력
							boolean flag2 = model.update(vo); // 구매 재고 확인
							if (flag2 == false) {
								view.cntF();// 재고 부족 출력
								continue; // 다시 입력 받을 수 있게 한다.
							}
							break;
						}

						// 결제하기
						AlcVO vo2 = new AlcVO(); // 객체 하나 더 만든 이유(price가져오기 위해서) > 사용자가 구매 수량 입력후 vo를 selectOne()하면
						// DB의재고가 사용자가 입력한 수량으로 SET되어버림
						vo2 = model.selectOne(vo); // 현재 vo에는 이름하고 수량 담겨있음, selcetOne은 이름 비교해서 그 같으면 그것을 반환함
						vo.setPrice(vo2.getPrice()); // 현재 vo2에는 모든 정보가 들어있음, 그 가격을 다시 vo에 set

						int total = vo.getCnt() * vo.getPrice();
						view.total(total);
						int balance = 0;

						while (true) {

							view.money(); // 돈 입력 받기
							int money = view.inputInt();
							balance = money - total; // 투입금액 - 총 합계금액 = 잔돈
							vo.setNum(balance);

							while (true) {
								if (total > money) { // 돈이 부족할때
									view.balF(balance); // 부족한금액 출력
									view.moneyAdd();
									money += view.inputInt();
									view.moneyIn(money);
									balance = money - total;
									vo.setNum(balance);
								}
								if (money >= total) { // 돈이 초과되거나 같으면 stop
									break;
								}
							}

							view.success();
							view.balT(total - money); // 잔돈 출력
							break;
						}

						boolean flag3 = view.receipt();
						if (flag3) {
							view.show_receipt(vo);// 영수증 보여줄게
							if(view.buyAgain()==true) { // 추가구매 한다면
								continue; // 자판기 메뉴출력(나이입력 진행 x)
							}
							else { // 추가구매 안한다면
								view.thanks();
								break; // 처음 실행부분(나이입력)으로 이동
							}

						}
						if(view.buyAgain()==false) { // 영수증 x 추가구매 x
							view.thanks(); // 구매 종료
							break;
						}
						continue;
					}


					else if (view.action == 3) { // 검색 (술이름으로 검색)
						AlcVO vo = new AlcVO();
						view.searchName();
						vo.setName(view.inputString());
						ArrayList<AlcVO> datas = model.selectAll(vo);
						view.menu(datas);

					} else if (view.action == 4) { // 프로그램 종료
						view.off();
						break;
					}

				}

			} 
			else if (view.age == 1234) { // 관리자 모드
				while (true) {
					view.admin_startView();

					if (view.action == 1) { // 술 추가 메뉴
						view.addDrink(); // 술 추가 메뉴입니다.

						AlcVO vo = new AlcVO(); // 세팅할 객체 생성
						CategoryVO cvo = new CategoryVO(); // 카테고리가 없는 종류는 추가를 할 수가 없어서 검사 진행을 위해
						ArrayList<CategoryVO> catedatas = model.categoryAll(cvo);
						ArrayList<AlcVO> datas = model.selectAll(vo);

						while (true) {
							view.category(); // 추가할 카테고리 입력해주세요

							int choice = view.categoryMenu(catedatas); // 대분류 카테고리 출력 후 int 반환
							vo.setCategory(catedatas.get(choice - 1).getName());
							if (datas != null) {
								break; // 카테고리가 있다면, 유효성검사 while문 탈출
							}
							else {
								view.admin_fail();
								continue; // 다시 입력 받도록 함
							}

						}

						while(true) { // 원래있는 음료를 추가하려고 하는 경우
							view.name();
							String name = view.inputString();  // 이름 입력
							vo.setName(name);

							ArrayList<AlcVO> datas1 = model.selectAll(vo);

							if(datas1.size() != 0) {
								view.admin_fail();
								continue;
							}
							break;
						}
						view.cnt(); 
						int cnt = view.inputInt(); // 재고입력
						vo.setCnt(cnt); 
						int price = view.price(); // 가격입력, 음수이거나 1000원 이하는 입력불가
						vo.setPrice(price);
						double abv = view.abv(); // 도수 입력, 음수이거나 100도 이상은 입력불가
						vo.setAbv(abv);


						if (model.insert(vo)) { // 술 추가
							view.admin_success();
							view.menu(datas);
						} else {
							view.admin_fail();
						}

					} else if (view.action == 2) { // 재고추가 메뉴
						AlcVO vo = new AlcVO();
						ArrayList<AlcVO> datas = model.selectAll(vo);
						view.menu(datas); // 전체메뉴 출력

						while(true) {
							view.soolpk(); // 술 번호 입력 받기
							int num;
							try {
								num = view.inputInt()-1;
								vo.setNum(datas.get(num).getNum()); // 재고추가할 술의 pk값 대입
							} catch (IndexOutOfBoundsException e) {
								view.indexWarn(datas);
								continue;
							}

							break;
						}

						view.cnt(); // 재고입력 받기
						vo.setCnt(-view.inputInt()); // 추가할 재고 입력 - 재고추가시에는 음수로 들어가야함
						if (model.update(vo)) { // 재고 추가
							view.admin_success();
							view.menu(datas);
						} else {
							view.admin_fail();
						}


					} else if (view.action == 3) { // 술 삭제 메뉴
						AlcVO vo = new AlcVO();
						ArrayList<AlcVO> datas = model.selectAll(vo);
						view.menu(datas); // 전체메뉴 출력
						while(true) {
							view.soolpk(); // 술 번호 입력 받기
							int num;
							try {
								num = view.inputInt();
								vo.setNum(datas.get(num - 1).getNum()); // 삭제할 술의 pk 대입
							} catch (IndexOutOfBoundsException e) {
								view.indexWarn(datas);
								continue;
							}
							break;
						}
						if (model.delete(vo)) { // 술 삭제
							view.admin_success();
							view.menu(datas);
						} else {
							view.admin_fail();
							view.menu(datas);
						}
					}  else if (view.action == 4) {
						view.machineOff();
						return;
					} else if (view.action == 5) {
						view.admin_off();
						break;
					}

					else {
						return;
					}
				}
			}
		}
	}
}