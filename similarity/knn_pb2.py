# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: knn.proto

import sys
_b=sys.version_info[0]<3 and (lambda x:x) or (lambda x:x.encode('latin1'))
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor.FileDescriptor(
  name='knn.proto',
  package='similarity',
  syntax='proto3',
  serialized_options=_b('\n\032com.google.step.similarityB\023OrganizationsProtos'),
  serialized_pb=_b('\n\tknn.proto\x12\nsimilarity\"\xbd\x01\n\rOrganizations\x12\x34\n\x04orgs\x18\x01 \x03(\x0b\x32&.similarity.Organizations.Organization\x1av\n\x0cOrganization\x12\n\n\x02id\x18\x01 \x01(\x05\x12\x42\n\tneighbors\x18\x02 \x03(\x0b\x32/.similarity.Organizations.Organization.Neighbor\x1a\x16\n\x08Neighbor\x12\n\n\x02id\x18\x01 \x01(\x05\x42\x31\n\x1a\x63om.google.step.similarityB\x13OrganizationsProtosb\x06proto3')
)




_ORGANIZATIONS_ORGANIZATION_NEIGHBOR = _descriptor.Descriptor(
  name='Neighbor',
  full_name='similarity.Organizations.Organization.Neighbor',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='id', full_name='similarity.Organizations.Organization.Neighbor.id', index=0,
      number=1, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=193,
  serialized_end=215,
)

_ORGANIZATIONS_ORGANIZATION = _descriptor.Descriptor(
  name='Organization',
  full_name='similarity.Organizations.Organization',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='id', full_name='similarity.Organizations.Organization.id', index=0,
      number=1, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='neighbors', full_name='similarity.Organizations.Organization.neighbors', index=1,
      number=2, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[_ORGANIZATIONS_ORGANIZATION_NEIGHBOR, ],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=97,
  serialized_end=215,
)

_ORGANIZATIONS = _descriptor.Descriptor(
  name='Organizations',
  full_name='similarity.Organizations',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='orgs', full_name='similarity.Organizations.orgs', index=0,
      number=1, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[_ORGANIZATIONS_ORGANIZATION, ],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=26,
  serialized_end=215,
)

_ORGANIZATIONS_ORGANIZATION_NEIGHBOR.containing_type = _ORGANIZATIONS_ORGANIZATION
_ORGANIZATIONS_ORGANIZATION.fields_by_name['neighbors'].message_type = _ORGANIZATIONS_ORGANIZATION_NEIGHBOR
_ORGANIZATIONS_ORGANIZATION.containing_type = _ORGANIZATIONS
_ORGANIZATIONS.fields_by_name['orgs'].message_type = _ORGANIZATIONS_ORGANIZATION
DESCRIPTOR.message_types_by_name['Organizations'] = _ORGANIZATIONS
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

Organizations = _reflection.GeneratedProtocolMessageType('Organizations', (_message.Message,), dict(

  Organization = _reflection.GeneratedProtocolMessageType('Organization', (_message.Message,), dict(

    Neighbor = _reflection.GeneratedProtocolMessageType('Neighbor', (_message.Message,), dict(
      DESCRIPTOR = _ORGANIZATIONS_ORGANIZATION_NEIGHBOR,
      __module__ = 'knn_pb2'
      # @@protoc_insertion_point(class_scope:similarity.Organizations.Organization.Neighbor)
      ))
    ,
    DESCRIPTOR = _ORGANIZATIONS_ORGANIZATION,
    __module__ = 'knn_pb2'
    # @@protoc_insertion_point(class_scope:similarity.Organizations.Organization)
    ))
  ,
  DESCRIPTOR = _ORGANIZATIONS,
  __module__ = 'knn_pb2'
  # @@protoc_insertion_point(class_scope:similarity.Organizations)
  ))
_sym_db.RegisterMessage(Organizations)
_sym_db.RegisterMessage(Organizations.Organization)
_sym_db.RegisterMessage(Organizations.Organization.Neighbor)


DESCRIPTOR._options = None
# @@protoc_insertion_point(module_scope)
