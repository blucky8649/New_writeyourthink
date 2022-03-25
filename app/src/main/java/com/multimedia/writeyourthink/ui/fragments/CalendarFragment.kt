package com.multimedia.writeyourthink.ui.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.facebook.login.LoginManager
import com.github.sundeepk.compactcalendarview.CompactCalendarView.CompactCalendarViewListener
import com.github.sundeepk.compactcalendarview.domain.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.multimedia.writeyourthink.ui.LoginActivity
import com.multimedia.writeyourthink.R
import com.multimedia.writeyourthink.databinding.FragmentCalendarBinding
import com.multimedia.writeyourthink.ui.DiaryActivity
import com.multimedia.writeyourthink.viewmodels.DiaryViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class CalendarFragment : Fragment(R.layout.fragment_calendar) {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DiaryViewModel

    private val simpleDateFormat = SimpleDateFormat("MMMM-YYYY", Locale.getDefault())
    private val DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private lateinit var myCalendar: Calendar

    /**
     * 데이터 관련
     */
    private var auth // 파이어 베이스 인증 객체
            : FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var userUID: String? = null
    var sdf: SimpleDateFormat? = null
    var c: Date? = null
    var df: SimpleDateFormat? = null
    var formattedDate: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        viewModel = (activity as DiaryActivity).viewModel

        viewModel.userInfo.observe(viewLifecycleOwner) {
            Glide.with(requireActivity()).load(it.userProfile).into(binding.ivProfile)
            binding.tvNickname.text = it.userName
        }
        myCalendar = Calendar.getInstance()

        viewModel.currentCalendarDate.observe(viewLifecycleOwner) {
            binding.calendarTitle.text = simpleDateFormat.format(it)
            binding.tvSelDate.text = DateFormat.format(it)
            binding.compactcalendarView.setCurrentDate(it)
        }

        viewModel.countDiaryContents.observe(viewLifecycleOwner) {
            Setdate(it, viewModel.currentCalendarDate.value ?: myCalendar.time, false)
            calendarlistener(it)
        }



        auth = FirebaseAuth.getInstance() // 파이어베이스 인증 객체 초기화.
        user = auth!!.currentUser
        userUID = user!!.uid


        binding.btnLogout.setOnClickListener {
            auth!!.signOut()
            LoginManager.getInstance().logOut()
            Toast.makeText(activity, getString(R.string.logout), Toast.LENGTH_SHORT).show()
            val intent1 = Intent(activity, LoginActivity::class.java)
            startActivity(intent1)
            requireActivity().finish()
        }
        binding.layoutLeft!!.setOnClickListener {
            binding.compactcalendarView.showCalendarWithAnimation()
            binding.compactcalendarView.showNextMonth()
        }
        binding.layoutRight!!.setOnClickListener {
            binding.compactcalendarView.showCalendarWithAnimation()
            binding.compactcalendarView.showPreviousMonth()
        }
        return binding.root
    }

    fun calendarlistener(countDate: HashMap<String, Int>) {
        binding.compactcalendarView.setListener(object : CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                viewModel.setCalendarTitle(dateClicked)
                val profile_counts = countDate[DateFormat!!.format(dateClicked)] ?: 0

                binding.tvSelDate.text = DateFormat.format(dateClicked)
                if (profile_counts!! > 0) {
                    binding.tvCount.text =
                        getString(R.string.frag3_2) + " : " + profile_counts + " " + getString(R.string.cases)
                } else {
                    binding.tvCount.text = getString(R.string.frag3_1)
                }
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                binding.compactcalendarView.removeAllEvents()
                Setdate(countDate, firstDayOfNewMonth, true)
            }
        })
    }

    //get current date
    fun Setdate(itemCount: HashMap<String, Int>, firstDayofNewMonth: Date, isSlide: Boolean) {
        c = firstDayofNewMonth

        df = SimpleDateFormat("yyyy-MM-dd")
        val profile_counts = itemCount[DateFormat.format(c)] ?: 0
        binding.compactcalendarView.setUseThreeLetterAbbreviation(true)
        sdf = SimpleDateFormat("MMMM yyyy")
        formattedDate = df!!.format(c)
        viewModel.setCalendarTitle(c!!)


        if (profile_counts!! > 0) {
            binding.tvCount.text =
                getString(R.string.frag3_2) + " : " + profile_counts + " " + getString(R.string.cases)
        } else {
            binding.tvCount.text = getString(R.string.frag3_1)
        }

        for (item in itemCount.keys) {
            val value = itemCount[item]
            myCalendar.set(Calendar.YEAR, item.substring(0, 4).toInt())
            myCalendar.set(Calendar.MONTH, item.substring(5,7).toInt() - 1)
            myCalendar.set(Calendar.DAY_OF_MONTH, item.substring(8).toInt())
            val event = Event(Color.BLUE, myCalendar.timeInMillis, "test")
            repeat(value!!) {

                binding.compactcalendarView.addEvent(event, false)
            }
        }
    }
    /**
     * Fragment에서 View Binding을 사용할 경우 Fragment는 View보다 오래 지속되어,
     * Fregment의 Lifecycle로 인해 메모리 누수가 발생할 수 있다.
     * 따라서 반드시 binding 변수를 onDestroyView() 이후에 null로 만들어 주어야한다.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}