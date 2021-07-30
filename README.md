# Selling_Used_Market

### NavogationView
~~~kotlin
// in activity_main.xml
<FrameLayout // for Fragment replace
	android:id="@+id/fragmentContainer"/>
<BottomNavigationView
	android:id="@+id/bottomNavigationView"
	app:menu="@menu/bottom_navigation_menu"
	app:itemIconTint="@drawable/select_menu_color"
	app:itemTextColor="@drawable/select_menu_color"
	app:itemRippleColor="@null" />
~~~
~~~kotlin
// bottom_navigation_menu.xml 생성
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">

    <item android:id="@+id/home"
        android:icon="@drawable/ic_baseline_home_24"
        android:title="홈"/>

    <item android:id="@+id/chat"
        android:icon="@drawable/ic_baseline_chat_24"
        android:title="채팅"/>

    <item android:id="@+id/myPage"
        android:icon="@drawable/ic_baseline_perm_identity_24"
        android:title="내정보"/>

</menu>
~~~

### NavigationView -> Fragment
~~~ kotlin
// HomeFragment.kt, ChatFragment.kt, MyPageFragment.kt 생성
// in MainActivity.kt
private val homeFragment = HomeFragment()
private val chatFragment = ChatFragment()
private val myPageFragment = MyPageFragment()

// Fragmnet 교체
private fun replaceFragment(fragment: Fragment) {
	supportFragmentManager.beginTransaction()
		.apply {
			replace(R.id.fragmentContainer, fragment)
			commit()
		}
}

// BottomNavigationView 아이템 선택
// R.id.fragmentContainer 를 해당 Fragment로 교체
private fun initBottomNavigationView() {
	bottomNavigationView.setOnNavigationItemSelectedListener {
		when (it.itemId) {
			R.id.home -> replaceFragment(homeFragment)
			R.id.chat -> replaceFragment(chatFragment)
			R.id.myPage -> replaceFragment(myPageFragment)
		}
		true
	}
}
~~~

### 프래그먼트 (Fragment)
View들을 Fragment 위에 생성한 것 (단독으로 사용될 수 없다.)
Activity에 속하고 Activity보다 가볍고 재활용성이 높다.
Activity > Fragment > View
~~~kotlin
// Fragment.kt 생성
class MyPageFragment: Fragment(R.layout.fragment_mypage) {

	private lateinit var binding: FragmentMypageBinding

	// 뷰가 그려지는 시점
	// 이때 코드 또는 데이터를 호출
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

		binding = FragmentMypageBinding.bind(view)
	}
	//화면에서 사라졌다가 다시 나타나는 시점
	// 주로 화면에 보여줄 값들을 초기화할때 사용
	override fun onStart() { }

	// onStart() 이후 화면 로딩이 완료되는 시점
	override fun onResume() {
		super.onResume()
		// recyclerView를 최신화
		articleAdapter.notifyDataSetChanged()
	}

	//뷰(View) 를 해제하게 되는 시점
    override fun onDestroyView() {
        super.onDestroyView()
		// DB listener를 제거
        articleDB.removeEventListener(listener)
    }
}
// onViewCreated -> onStart -> onResume -> onDestroyView -> onViewCreated
~~~

### RecyclerView
DB  설계 시, diffUtil에 사용할 적절한 (primary)key 설계가 중요
~~~kotlin
// in Activity.kt
binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
binding.chatRecyclerView.adapter = chatRoomAdapter
// in Fragment.kt
binding.articleRecyclerView.layoutManager = LinearLayoutManager(context)
binding.articleRecyclerView.adapter = articleAdapter
~~~
~~~kotlin
// reycyclerView의 item 클릭 시 동작(activity 이동 등)을 정의
class ArticleAdapter(val onItemClicked: (ArticleModel) -> Unit): ListAdapter<ArticleModel, ArticleAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemArticleBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(articleModel: ArticleModel) {

			// adpater를 호출하는 activity에서 onItemClicked 람다식을 작성
            binding.root.setOnClickListener {
                onItemClicked(articleModel)
            }
        }
	}
~~~
~~~kotlin
// reycyclerView의 item을 눌렀을때 onItemClicked 호출
articleAdapter = ArticleAdapter(onItemClicked = { articleModel ->
	// 액티비티 이동 또는 기타 실행할 코드
})
// 또는 아래처럼 람다식을 열어도 된다.
articleAdapter = ArticleAdapter { it(= articleModel) ->
	// 액티비티 이동 또는 기타 실행할 코드
}
~~~

### Firebase Auth
~~~kotlin
private val auth = Firebase.auth
// createUserWithEmailAndPassword 성공 시 자동로그인
auth.createUserWithEmailAndPassword(email, password)
	// fragment에서는 requireActivity() / Activity에서는 this
	.addOnCompleteListener(requireActivity()) { task ->
		if (task.isSuccessful) {
			// 계정 생성 성공
		} else {
			// 계정 생성 실패
		}
	}

auth.signInWithEmailAndPassword(email, password)
	.addOnCompleteListener(requireActivity()) { task ->
		if (task.isSuccessful) {
			// 로그인 성공
		} else {
			// 로그인 실패
		}
	}
~~~

### Firebase Realtime Database
1. addValueEventListener() 데이터 경로 전체를 읽고 변경사항에 대해 수신대기
2. addListenerForSingleValueEvent() 한번만 호출되고 수신대기 할 필요 없는 데이터에 사용
3. ddChildEventListener() 경로의 특정 child의 변경사항에 대해 수신대기
~~~kotlin
private lateinit var chatDB: DatabaseReference
// child로 자식 노드에 접근 (없으면 생성)
chatDB = Firebase.database.reference.child("chat")

chatDB.addListenerForSingleValueEvent(object : ValueEventListener {
	override fun onDataChange(snapshot: DataSnapshot) {
		// children 으로 자식노드들을 리스트 형태로 호출
		snapshot.children.forEach {
			val model = it.getValue(ChatListItem::class.java)
			model ?: return
			chatRoomList.add(model)
		}
		chatAdapter.submitList(chatRoomList)
		chatAdapter.notifyDataSetChanged()
	}

	override fun onCancelled(error: DatabaseError) {
		TODO("Not yet implemented")
	}

})
~~~