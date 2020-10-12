package swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class Othello implements MouseListener {
	Map<String, String> gameRecord;//盤面情報
	private final String[] gameRecordKeys;
	List<String> gouhousyuArray;//合法手配列
	JLabel[][] masuArray;//マス配列
	JLabel tebanLabel;//手番ラベル
	JLabel stoneNumLabel;//石の数ラベル
	JLabel gameInfoLabel;//パス,勝敗表示ラベル
	JButton passButton;//パスボタン
	private String teban;
	private String currentMasu;//クリックしたマス
	private int blackNum;//黒石
	private int whiteNum;//白石
	private boolean gameEndFlg;//決着フラグ
	private boolean passFlg;//連続パスフラグ

	Othello(){
		this.gameRecordKeys = new String[]{ "d1s1", "d1s2", "d1s3", "d1s4", "d1s5", "d1s6", "d1s7", "d1s8",
				"d2s1", "d2s2", "d2s3", "d2s4", "d2s5", "d2s6", "d2s7", "d2s8",
				"d3s1", "d3s2", "d3s3", "d3s4", "d3s5", "d3s6", "d3s7", "d3s8",
				"d4s1", "d4s2", "d4s3", "d4s4", "d4s5", "d4s6", "d4s7", "d4s8",
				"d5s1", "d5s2", "d5s3", "d5s4", "d5s5", "d5s6", "d5s7", "d5s8",
				"d6s1", "d6s2", "d6s3", "d6s4", "d6s5", "d6s6", "d6s7", "d6s8",
				"d7s1", "d7s2", "d7s3", "d7s4", "d7s5", "d7s6", "d7s7", "d7s8",
				"d8s1", "d8s2", "d8s3", "d8s4", "d8s5", "d8s6", "d8s7", "d8s8" };
		this.teban = "black";
		this.currentMasu = "";
		this. blackNum = 0;
		this. whiteNum = 0;
		this. gameEndFlg=false;
		this. passFlg=false;
	}

	public static void main(String[] args) {
		Othello othello = new Othello();
		othello.run();
	}

	public void run() {
		JFrame frame = new JFrame("オセロ");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//閉じるボタンを押した時に終了する
		frame.setSize(700, 800);//画面サイズwh
		frame.setLocation(1150, 100);//画面の立ち上がり位置
		setUpJPanel(frame);//JPanelの準備
		setUpGameRecord();//盤面情報の初期化
		setUpStone();//初期石の配置
		updateLabelText();
		frame.setVisible(true);//画面に見えるようにする
	}

	public void setUpJPanel(JFrame frame) {
		//JPanelの準備
		JPanel p = new JPanel();
		frame.setContentPane(p);
		p.addMouseListener(this);//マウスクリックイベントの設定
		p.setLayout(null);//レイアウト設定の初期化
		//手番表示ラベル
		int marginX = 50;//余白X
		int marginY = 30;//余白Y
		int tdW = 120;//表示画面の横幅
		int tdH = 35;//表示画面の高さ
		tebanLabel = new JLabel();
		tebanLabel.setBounds(marginX, marginY, tdW, tdH);
		tebanLabel.setFont(new Font("MSゴシック", Font.PLAIN, 24));
		tebanLabel.setOpaque(true);
		tebanLabel.setBackground(Color.white);//バックグラウンドカラーの設定
		tebanLabel.setHorizontalAlignment(JLabel.CENTER);//水平位置
		p.add(tebanLabel);//手番表示ラベル
		//石ラベル
		stoneNumLabel = new JLabel();
		stoneNumLabel.setBounds(marginX, 75, 180, tdH);
		stoneNumLabel.setFont(new Font("MSゴシック", Font.PLAIN, 24));
		stoneNumLabel.setOpaque(true);
		stoneNumLabel.setBackground(Color.white);//バックグラウンドカラーの設定
		stoneNumLabel.setHorizontalAlignment(JLabel.CENTER);//水平位置
		p.add(stoneNumLabel);//手番表示ラベル
		//パス,勝敗表示ラベル
		gameInfoLabel = new JLabel();
		//gameInfoLabel.setText("パスしてください。");
		//gameInfoLabel.setText("黒の勝ちです。");
		gameInfoLabel.setBounds(marginX + tdW + 10, marginY, 240, tdH);
		gameInfoLabel.setFont(new Font("MSゴシック", Font.PLAIN, 24));
		gameInfoLabel.setOpaque(true);
		//gameInfoLabel.setBackground(Color.white);//バックグラウンドカラーの設定
		gameInfoLabel.setHorizontalAlignment(JLabel.CENTER);//水平位置
		p.add(gameInfoLabel);
		//パスボタン
		passButton = new JButton("パス");
		passButton.setFont(new Font("MSゴシック", Font.PLAIN, 24));
		passButton.setBounds(marginX + 180 + 10, 75, 100, tdH);
		//ボタンを押した時のイベント処理
		passButton.addActionListener(e -> {
			passButtonEvent();
		});
		p.add(passButton);

		//オセロ盤(マスの作成)
		int banMarginX = 50;//余白X
		int banMarginY = 120;//余白Y
		int masuW = 75;//表示画面の横幅
		int masuH = 75;//表示画面の高さ
		LineBorder br = new LineBorder(Color.black, 2, true);//罫線の色,太さ
		//マス64個の配置位置(X8行＊Y8列)//罫線を考慮
		int[] setX = { banMarginX, 125 - 2, 200 - 4, 275 - 6, 350 - 8, 425 - 10, 500 - 12, 575 - 14 };
		int[] setY = { banMarginY, 195 - 2, 270 - 4, 345 - 6, 420 - 8, 495 - 10, 570 - 12, 645 - 14 };
		masuArray = new JLabel[8][8];
		//マスの配置＆設定
		for (int y = 0; y < masuArray.length; y++) {
			for (int x = 0; x < masuArray[0].length; x++) {
				masuArray[x][y] = new JLabel("");
				masuArray[x][y].setBounds(setX[x], setY[y], masuW, masuH);//配置x,配置y,width,height
				masuArray[x][y].setFont(new Font("MSゴシック", Font.PLAIN, 71));//フォントスタイル,文字の大きさ
				masuArray[x][y].setOpaque(true);//バックグラウンドカラーの設定を許可する。
				masuArray[x][y].setBackground(Color.green);//バックグラウンドカラーの設定
				masuArray[x][y].setBorder(br);
				masuArray[x][y].setHorizontalAlignment(JLabel.CENTER);//水平位置
				p.add(masuArray[x][y]);//盤の配置
			}
		}
	}

	public void setUpGameRecord() {
		//盤面情報の初期化
		gameRecord = new HashMap<>();
		for (int i = 0; i < gameRecordKeys.length; i++) {
			if ((gameRecordKeys[i].equals("d4s4")) || (gameRecordKeys[i].equals("d5s5"))) {
				gameRecord.put(gameRecordKeys[i], "white");
			} else if ((gameRecordKeys[i].equals("d4s5")) || (gameRecordKeys[i].equals("d5s4"))) {
				gameRecord.put(gameRecordKeys[i], "black");
			} else {
				gameRecord.put(gameRecordKeys[i], "None");
			}
		}

	}

	public void setUpStone() {
		//初期石の配置
		masuArray[3][3].setText("●");
		masuArray[3][3].setForeground(Color.white);//石の色
		masuArray[4][4].setText("●");
		masuArray[4][4].setForeground(Color.white);
		masuArray[3][4].setText("●");
		masuArray[3][4].setForeground(Color.black);
		masuArray[4][3].setText("●");
		masuArray[4][3].setForeground(Color.black);
	}

	public void changeTeban() {
		if (teban == "black") {
			teban = "white";
		} else if (teban == "white") {
			teban = "black";
		}
		updateLabelText();
	}

	public void updateLabelText() {
		String tebanDisplay = "";
		if (teban.equals("black")) {
			tebanDisplay = "黒";
		} else if (teban.equals("white")) {
			tebanDisplay = "白";
		}
		tebanLabel.setText("手番：" + tebanDisplay);
		checkStoneNum();
	}

	public void checkStoneNum() {
		//石の数の確認をし,石の数のテキストを更新する。
		int tempBlackNum = 0;
		int tempWhiteNum = 0;
		for (String val : gameRecord.values()) {
			if (val.equals("black")) {
				tempBlackNum++;
			} else if (val.equals("white")) {
				tempWhiteNum++;
			}
		}
		setBlackNum(tempBlackNum);
		setWhiteNum(tempWhiteNum);
		stoneNumLabel.setText("黒：" + getBlackNum() + "  " + "白：" + getWhiteNum());
	}

	public void setGouhousyuArray() {
		//手番の合法手をセットする。
		//8方向探索用配列
		int[][] allDirectionArray = { { -1, 0 }, { -1, 1 }, { 0, 1 }, { 1, 1 }, { 1, 0 }, { 1, -1 }, { 0, -1 },
				{ -1, -1 } };//8方向(上,右上,右,右下,下,左下,左,左上)
		List<String> tempGouhousyuArray = new ArrayList<String>();//合法手仮格納配列
		String[] switchArray = null;//手番により切り替え
		String[] useBlackArray = { "black", "white" };//手番黒用
		String[] useWhiteArray = { "white", "black" };//手番白用
		int targetDan, targetSuji, checkDan, checkSuji;
		String checkMasu;
		boolean existRivalStoneFlg;//ライバルの石が間に存在するか？

		if (gouhousyuArray != null) {
			gouhousyuArray.clear();//配列のリセット
		}
		if (teban.equals("black")) {
			switchArray = useBlackArray;
		} else if (teban.equals("white")) {
			switchArray = useWhiteArray;
		}
		for (int i = 0; i < gameRecordKeys.length; i++) {
			//gameRecordKeys[i]:合法手確認の対象のマス
			if (!(gameRecord.get(gameRecordKeys[i]).equals("None"))) {
				continue;//合法手確認の対象のマスに石があれば抜ける
			}
			targetDan = Integer.parseInt(gameRecordKeys[i].substring(1, 2));//二文字目の段の切り出し
			targetSuji = Integer.parseInt(gameRecordKeys[i].substring(3, 4));//四文字目の筋の切り出し
			for (int j = 0; j < allDirectionArray.length; j++) {
				existRivalStoneFlg = false;//ライバルの石が間に存在しないフラグをFalseにする
				checkDan = targetDan;//new Integer()//コピー
				checkSuji = targetSuji;
				while (true) {
					checkDan += allDirectionArray[j][0];
					checkSuji += allDirectionArray[j][1];
					checkMasu = 'd' + String.valueOf(checkDan) + 's' + String.valueOf(checkSuji);
					if ((checkDan == 0) || (checkSuji == 0) || (checkDan == 9) || (checkSuji == 9)) {
						break;//盤外であれば抜ける
					} else {
						//盤内であれば
						if (gameRecord.get(checkMasu).equals("None")) {
							break;//一マス先に石がなければ抜ける
						}
						if ((existRivalStoneFlg == false) && (gameRecord.get(checkMasu).equals(switchArray[0]))) {
							//[0]:自石
							break;//#間にライバルの石がない＆一マス先が自石ならぬける
						}
						if (gameRecord.get(checkMasu).equals(switchArray[1])) {
							//[1]:ライバルの石
							existRivalStoneFlg = true;
							continue;//マスの確認方向を一マス伸ばし処理を続ける
						}
						if ((existRivalStoneFlg) && (gameRecord.get(checkMasu).equals(switchArray[0]))) {
							//[0]:自石
							tempGouhousyuArray.add(gameRecordKeys[i]);//合法手を配列に格納
							existRivalStoneFlg = false;//フラグをFalseに戻す
							break;//ループを抜ける
						}
					}
				}
			}
		}
		gouhousyuArray = new ArrayList<String>(new HashSet<>(tempGouhousyuArray));//配列から重複した値を削除する
	}

	public void turnOverStone(String startingPoint) {
		//着手＆石を反転させる。
		//startingPoint:着手を起点にする。
		//8方向探索用配列
		int[][] allDirectionArray = { { -1, 0 }, { -1, 1 }, { 0, 1 }, { 1, 1 }, { 1, 0 }, { 1, -1 }, { 0, -1 },
				{ -1, -1 } };//8方向(上,右上,右,右下,下,左下,左,左上)
		boolean turnOverFlg;//反転動作確認に使用
		List<String> turnOverStoneArray = new ArrayList<String>();//反転対象配列
		String[] switchArray = null;//手番により切り替え
		String[] useBlackArray = { "black", "white" };//手番黒用
		String[] useWhiteArray = { "white", "black" };//手番白用
		int targetDan, targetSuji, checkDan, checkSuji, targetX, targetY;
		String checkMasu;
		if (teban.equals("black")) {
			switchArray = useBlackArray;
		} else if (teban.equals("white")) {
			switchArray = useWhiteArray;
		}
		//石の反転
		targetDan = Integer.parseInt(startingPoint.substring(1, 2));//二文字目の段の切り出し
		targetSuji = Integer.parseInt(startingPoint.substring(3, 4));//四文字目の筋の切り出し
		for (int j = 0; j < allDirectionArray.length; j++) {
			turnOverFlg = false;//反転動作確認に使用
			checkDan = targetDan;
			checkSuji = targetSuji;
			while (true) {
				checkDan += allDirectionArray[j][0];
				checkSuji += allDirectionArray[j][1];
				checkMasu = 'd' + String.valueOf(checkDan) + 's' + String.valueOf(checkSuji);
				if ((checkDan == 0) || (checkSuji == 0) || (checkDan == 9) || (checkSuji == 9)) {
					turnOverStoneArray.clear();//配列のリセット
					break;//盤外であれば抜ける
				}
				//盤内であれば
				if (gameRecord.get(checkMasu).equals("None")) {
					turnOverStoneArray.clear();
					break;//一マス先に石がなければ抜ける
				}
				if ((turnOverFlg == false) && (gameRecord.get(checkMasu).equals(switchArray[0]))) {
					//[0]:自石
					turnOverStoneArray.clear();
					break;//間にライバルの石がない＆一マス先が自石ならぬける
				}
				if (gameRecord.get(checkMasu).equals(switchArray[1])) {
					//[1]:ライバルの石
					turnOverFlg = true;
					turnOverStoneArray.add(checkMasu);//反転対象の石が置かれているマスを配列に格納する
					continue;//マスの確認方向を一マス伸ばし処理を続ける
				}
				if ((turnOverFlg) && (gameRecord.get(checkMasu).equals(switchArray[0]))) {
					//[0]:自石
					//配列をもとに反転させる
					System.out.println("反転対象配列：" + turnOverStoneArray);

					for (int i = 0; i < turnOverStoneArray.size(); i++) {
						targetY = Integer.parseInt(turnOverStoneArray.get(i).substring(1, 2));//二文字目の段の切り出し
						targetX = Integer.parseInt(turnOverStoneArray.get(i).substring(3, 4));//四文字目の筋の切り出し
						masuArray[targetX - 1][targetY - 1].setText("●");
						if (switchArray[0].equals("black")) {
							masuArray[targetX - 1][targetY - 1].setForeground(Color.black);
						} else if (switchArray[0].equals("white")) {
							masuArray[targetX - 1][targetY - 1].setForeground(Color.white);
						}
						gameRecord.replace(turnOverStoneArray.get(i), switchArray[0]);//盤面情報の更新
					}
					turnOverFlg = false;//フラグをFalseに戻す
					break;//ループを抜ける
				}
			}
		}
		return;
	}

	public void winLoseJudgment(int passEnd) {
		//着手完了後に、勝敗が着いているか調べる。勝敗が着いている場合は、手番テキストを更新し、終了フラグを立てる。
		//passEnd==1:連続パスによる終了
		//盤面に石の置ける場所がない。又は、石の数が0。であれば終了
		int tempNoneNum = 0;
		int tempBlackNum = 0;
		int tempWhiteNum = 0;
		for (int i = 0; i < gameRecordKeys.length; i++) {
			if (gameRecord.get(gameRecordKeys[i]).equals("None")) {
				tempNoneNum++;
			} else if (gameRecord.get(gameRecordKeys[i]).equals("black")) {
				tempBlackNum++;
			} else if (gameRecord.get(gameRecordKeys[i]).equals("white")) {
				tempWhiteNum++;
			}
		}
		if ((passEnd == 1) || (tempNoneNum == 0) || (tempBlackNum == 0) || (tempWhiteNum == 0)) {
			gameEndFlg = true;//決着フラグ
		}
		if (passEnd == 1){
			System.out.println("連続パスによりゲームを終了します。");
		}
		if (gameEndFlg) {
			if ((tempWhiteNum == 0) || (tempBlackNum > tempWhiteNum)) {
				gameInfoLabel.setText("黒の勝ちです。");
				gameInfoLabel.setBackground(Color.white);//バックグラウンドカラーの設定
				return;
			}
			if ((tempBlackNum == 0) || (tempBlackNum < tempWhiteNum)) {
				gameInfoLabel.setText("白の勝ちです。");
				gameInfoLabel.setBackground(Color.white);//バックグラウンドカラーの設定
				return;
			}
			if (tempBlackNum == tempWhiteNum) {
				gameInfoLabel.setText("引き分けです。");
				gameInfoLabel.setBackground(Color.white);//バックグラウンドカラーの設定
				return;
			}
		}
	}

	public void passButtonEvent() {
		System.out.println("パスボタンを押しました。");
		if(gameEndFlg){
			return;
		}
		setGouhousyuArray();
		if(gouhousyuArray.size()!=0){
			System.out.println("合法手があります。パス出来ません。");
			return;
		}else{
			gameInfoLabel.setText("");
			if(passFlg) {
				//連続パスにより終了
				winLoseJudgment(1);
				return;
			}else {
				passFlg=true;
				changeTeban();
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(gameEndFlg) {
			//決着がついている。
			return;
		}
		setGouhousyuArray();//手番の合法手をセットする。
		if(gouhousyuArray.size()==0){
			System.out.println("合法手がありません。パスしてください。");
			gameInfoLabel.setText("パスしてください。");
			return;
		}
		System.out.println("合法手" + gouhousyuArray);
		Point point = e.getPoint();
		//System.out.println("p：" + point);
		//x:52～123,125～196,198～269,271～342,344～415,417～488,490～561,563～634
		//y:122～193,195～266,268～339,341～412,414～485,487～558,560～631,633～704
		String clickX;
		String clickY;
		if ((point.x >= 52) && (point.x <= 123)) {
			clickX = "1";
		} else if ((point.x >= 125) && (point.x <= 196)) {
			clickX = "2";
		} else if ((point.x >= 198) && (point.x <= 269)) {
			clickX = "3";
		} else if ((point.x >= 271) && (point.x <= 342)) {
			clickX = "4";
		} else if ((point.x >= 344) && (point.x <= 415)) {
			clickX = "5";
		} else if ((point.x >= 417) && (point.x <= 488)) {
			clickX = "6";
		} else if ((point.x >= 490) && (point.x <= 561)) {
			clickX = "7";
		} else if ((point.x >= 563) && (point.x <= 634)) {
			clickX = "8";
		} else {
			clickX = "0";
		}
		if ((point.y >= 122) && (point.y <= 193)) {
			clickY = "1";
		} else if ((point.y >= 195) && (point.y <= 266)) {
			clickY = "2";
		} else if ((point.y >= 268) && (point.y <= 339)) {
			clickY = "3";
		} else if ((point.y >= 341) && (point.y <= 412)) {
			clickY = "4";
		} else if ((point.y >= 414) && (point.y <= 485)) {
			clickY = "5";
		} else if ((point.y >= 487) && (point.y <= 558)) {
			clickY = "6";
		} else if ((point.y >= 560) && (point.y <= 631)) {
			clickY = "7";
		} else if ((point.y >= 633) && (point.y <= 704)) {
			clickY = "8";
		} else {
			clickY = "0";
		}
		if ((clickX.equals("0")) || (clickY.equals("0"))) {
			currentMasu = "盤外";//盤外
		} else {
			currentMasu = "d" + clickY + "s" + clickX;
		}
		System.out.println("クリックしたマス：" + currentMasu);
		//合法手確認
		if (gouhousyuArray.contains(currentMasu)) {
			masuArray[Integer.parseInt(clickX) - 1][Integer.parseInt(clickY) - 1].setText("●");
			if (teban.equals("black")) {
				masuArray[Integer.parseInt(clickX) - 1][Integer.parseInt(clickY) - 1].setForeground(Color.black);
			} else if (teban.equals("white")) {
				masuArray[Integer.parseInt(clickX) - 1][Integer.parseInt(clickY) - 1].setForeground(Color.white);
			}
			gameRecord.replace(currentMasu, teban);//盤面情報の更新
			turnOverStone(currentMasu);//石の反転
			passFlg=false;
			changeTeban();//手番の交代
			winLoseJudgment(0);//決着が着いているか？
		} else {
			System.out.println("合法手ではありません");
			return;
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}



	public Map<String, String> getGameRecord() {
		return gameRecord;
	}

	public void setGameRecord(Map<String, String> gameRecord) {
		this.gameRecord = gameRecord;
	}

	public String[] getGameRecordKeys() {
		return gameRecordKeys;
	}


	public List<String> getGouhousyuArray() {
		return gouhousyuArray;
	}

	public void setGouhousyuArray(List<String> gouhousyuArray) {
		this.gouhousyuArray = gouhousyuArray;
	}

	public JLabel[][] getMasuArray() {
		return masuArray;
	}

	public void setMasuArray(JLabel[][] masuArray) {
		this.masuArray = masuArray;
	}

	public JLabel getTebanLabel() {
		return tebanLabel;
	}

	public void setTebanLabel(JLabel tebanLabel) {
		this.tebanLabel = tebanLabel;
	}

	public JLabel getStoneNumLabel() {
		return stoneNumLabel;
	}

	public void setStoneNumLabel(JLabel stoneNumLabel) {
		this.stoneNumLabel = stoneNumLabel;
	}

	public JLabel getGameInfoLabel() {
		return gameInfoLabel;
	}

	public void setGameInfoLabel(JLabel gameInfoLabel) {
		this.gameInfoLabel = gameInfoLabel;
	}

	public JButton getPassButton() {
		return passButton;
	}

	public void setPassButton(JButton passButton) {
		this.passButton = passButton;
	}

	public String getTeban() {
		return teban;
	}

	public void setTeban(String teban) {
		this.teban = teban;
	}

	public String getCurrentMasu() {
		return currentMasu;
	}

	public void setCurrentMasu(String currentMasu) {
		this.currentMasu = currentMasu;
	}

	public int getBlackNum() {
		return blackNum;
	}

	public void setBlackNum(int blackNum) {
		this.blackNum = blackNum;
	}

	public int getWhiteNum() {
		return whiteNum;
	}

	public void setWhiteNum(int whiteNum) {
		this.whiteNum = whiteNum;
	}

	public boolean isGameEndFlg() {
		return gameEndFlg;
	}

	public void setGameEndFlg(boolean gameEndFlg) {
		this.gameEndFlg = gameEndFlg;
	}

	public boolean isPassFlg() {
		return passFlg;
	}

	public void setPassFlg(boolean passFlg) {
		this.passFlg = passFlg;
	}



}
