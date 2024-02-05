package com.example.assignment2

import android.content.ContentValues
import android.os.Bundle
import android.provider.ContactsContract
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.itemsIndexed
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
//import androidx.compose.material3.icons.Icons
//import androidx.compose.material3.icons.filled.Add
//import androidx.compose.material3.icons.filled.Person
//import androidx.compose.material3.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.assignment2.ui.theme.ContentProviderDemoTheme
import androidx.compose.ui.text.input.ImeAction


data class Contact(val name: String, val number: String)

class MainActivity : ComponentActivity() {
    private var _contactsState by mutableStateOf(emptyList<Contact>())
    val contactsState: List<Contact> get() = _contactsState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ContentProviderDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ContactManager()
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ContactManager() {
    var contactName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }

    // Introduce a new variable to store fetched contacts separately
    var contacts by remember { mutableStateOf(emptyList<Contact>()) }

    val contactsState by remember { mutableStateOf(emptyList<Contact>()) }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        OutlinedTextField(
            value = contactName,
            onValueChange = { contactName = it },
            label = { Text("Contact Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            )
        )

        OutlinedTextField(
            value = contactNumber,
            onValueChange = { contactNumber = it },
            label = { Text("Contact Number") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { fetchContacts(context) { newContacts -> contacts = newContacts } },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fetch Contacts")
            }

            Button(
                onClick = { addContact(context, contactName, contactNumber) { newContact ->
                    contacts = contacts + newContact
                } },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Contact")
            }
        }

        LazyColumn {
            itemsIndexed(contacts) { index, contact ->
                ContactItem(contact)
            }
        }

        AboutSection()
    }
}

@Composable
fun ContactItem(contact: Contact) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        //verticalAlignment = Alignment.CenterVertically
    ) {
        //Icon(imageVector = Icons.Default.Person, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = "Name: ${contact.name}", fontWeight = FontWeight.Bold)
            Text(text = "Number: ${contact.number}")
        }
    }
}

@Composable
fun AboutSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        //horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon(imageVector = Icons.Default.Info, contentDescription = null)
        Spacer(modifier = Modifier.height(8.dp))
        Text("About")
        Spacer(modifier = Modifier.height(4.dp))
        Text("Student Name: Saroj Kunwar")
        Text("Student ID:301365787")
    }
}
fun fetchContacts(context: android.content.Context, onContactsFetched: (List<Contact>) -> Unit) {
    val projection = arrayOf(
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )

    val cursor = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        projection,
        null,
        null,
        null
    )

    val newContacts = mutableListOf<Contact>()

    cursor?.use {
        while (it.moveToNext()) {
            val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            val name = it.getString(nameIndex)
            val number = it.getString(numberIndex)

            newContacts.add(Contact(name, number))
        }
    }

    cursor?.close()

    try {
        onContactsFetched(newContacts)
    } catch (e: Exception) {
        Log.e("ContactManager", "Error fetching contacts: ${e.message}", e)
    }
}


fun addContact(context: android.content.Context, name: String, number: String, onContactAdded: (Contact) -> Unit) {
    val contentValues = ContentValues().apply {
        put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
        put(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, name)
        put(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
    }

    val rawContactUri = context.contentResolver.insert(
        ContactsContract.RawContacts.CONTENT_URI,
        contentValues
    )

    val rawContactId = rawContactUri?.lastPathSegment?.toLongOrNull()

    if (rawContactId != null) {
        // Adding contact successful, update the state
        onContactAdded(Contact(name, number))
    }
}
